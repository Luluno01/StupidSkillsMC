package vip.untitled.stupidskills.effects

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import vip.untitled.stupidskills.helpers.TickCounter
import java.math.BigInteger
import java.util.*

open class StupidEffect(protected val context: JavaPlugin, protected val key: NamespacedKey) : EffectAdapter(), Effect,
    Listener {
    companion object {
        const val maxLevel = 10
        const val minDuration = 20 * 60
        const val maxDuration = 20 * 90
        const val extraTicksPerLevel = (maxDuration - minDuration) / (maxLevel - 1.0)
        protected var instance: StupidEffect? = null
        fun getInstance(context: JavaPlugin? = null, key: NamespacedKey? = null): StupidEffect {
            if (instance == null) {
                instance = StupidEffect(context!!, key!!)
            }
            return instance!!
        }
    }

    override val maxLevel = Companion.maxLevel

    protected val clearStupidityTasks = hashMapOf<UUID, ClearStupidityTask>()

    open class StupidMetadataValue(plugin: JavaPlugin, val level: Int) : MetadataValueAdapter(plugin) {
        override fun value(): Int {
            return level
        }

        override fun invalidate() {
            return
        }
    }

    open class RemainingStupidDurationMetadataValue(plugin: JavaPlugin, private val duration: Int) :
        MetadataValueAdapter(plugin) {
        override fun value(): Int {
            return duration
        }

        override fun invalidate() {
            return
        }
    }

    @Suppress("LeakingThis")
    open class ClearStupidityTask(
        val plugin: JavaPlugin,
        val effect: StupidEffect,
        val entity: LivingEntity,
        private val metaKey: String,
        val duration: Long
    ) : BukkitRunnable() {
        open val targetTick =
            (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick + BigInteger.valueOf(duration)

        init {
            effect.clearStupidityTasks[entity.uniqueId]?.cancel()
            effect.clearStupidityTasks[entity.uniqueId] = this
            this.runTaskLater(plugin, duration)
        }

        open val remainingTicks: Long
            get() {
                val ticks = targetTick - (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick
                return if (ticks.compareTo(BigInteger.ZERO) == -1) 0
                else ticks.toLong()
            }

        override fun run() {
            effect.clearStupidityTasks.remove(entity.uniqueId)
            if (entity.isDead) return  // Dead for being stupid?
            entity.removeMetadata(metaKey, plugin)
        }

        override fun cancel() {
            effect.clearStupidityTasks.remove(entity.uniqueId)
            super.cancel()
        }
    }

    open fun setRemainingStupidMetadata(entity: LivingEntity, duration: Int) {
        entity.setMetadata("$key-remaining", RemainingStupidDurationMetadataValue(context, duration))
    }

    open fun getRemainingStupidMetadata(entity: LivingEntity): RemainingStupidDurationMetadataValue? {
        for (meta in entity.getMetadata("$key-remaining")) {
            if (meta.owningPlugin == context && meta is RemainingStupidDurationMetadataValue) return meta
        }
        return null
    }

    open fun clearRemainingStupidMetadata(entity: LivingEntity) {
        entity.removeMetadata("$key-remaining", context)
    }

    open fun setStupidMetadata(entity: LivingEntity, level: Int) {
        entity.setMetadata(key.toString(), StupidMetadataValue(context, level))
    }

    open fun getStupidMetadata(entity: LivingEntity): StupidMetadataValue? {
        for (meta in entity.getMetadata(key.toString())) {
            if (meta.owningPlugin == context && meta is StupidMetadataValue) return meta
        }
        return null
    }

    open fun clearStupidMetadata(entity: LivingEntity) {
        entity.removeMetadata(key.toString(), context)
    }

    @EventHandler
    open fun onPlayerJoin(event: PlayerJoinEvent) {
        // Check if the player has remaining ticks
        val player = event.player
        val level: Int? = getStupidMetadata(player)?.value()
        val remainingTicks: Int? = getRemainingStupidMetadata(player)?.value()
        if (level == null) {
            if (remainingTicks != null) {
                context.logger.warning("[StupidEffect] Player ${player.displayName} has $remainingTicks remaining tick(s), but stupid level is not specified")
                clearRemainingStupidMetadata(player)
            }
        } else {
            if (remainingTicks == null) {
                context.logger.warning("[StupidEffect] Player ${player.displayName} has a stupid level $level, but no remaining tick, restarting timer")
                ClearStupidityTask(context, this, player, key.toString(), getDuration(level).toLong())
            } else {
                context.logger.info("[StupidEffect] Player ${player.displayName} continues being stupid for $remainingTicks ticks")
                clearRemainingStupidMetadata(player)
                ClearStupidityTask(context, this, player, key.toString(), remainingTicks.toLong())
            }
        }
    }

    @EventHandler
    open fun onPlayerExit(event: PlayerQuitEvent) {
        // Cancel the timer and save remaining ticks
        val player = event.player
        val task = clearStupidityTasks[player.uniqueId]
        if (task != null) {
            setRemainingStupidMetadata(player, task.remainingTicks.toInt())
            task.cancel()
            clearStupidityTasks.remove(player.uniqueId)
            context.logger.info("Saving remaining ticks for player ${player.displayName}")
        }
    }

    open fun getDuration(level: Int): Int {
        return getLeveledValue(minDuration.toDouble(), extraTicksPerLevel, sanitizeLevel(level)).toInt()
    }

    open fun getStupidity(entity: LivingEntity): Int {
        val meta = getStupidMetadata(entity)
        return meta?.value() ?: 0
    }

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        if (entity !is LivingEntity) return
        val stupidity = getStupidity(entity)
        if (stupidity > level) return  // Don not override higher level
        val lvl = sanitizeLevel(level)
        val duration = getDuration(lvl)
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING,
                duration,
                lvl,
                true,
                true,
                true
            ), true
        )
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOW,
                duration,
                lvl,
                true,
                true,
                true
            ), true
        )
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.WEAKNESS,
                duration,
                lvl,
                true,
                true,
                true
            ), true
        )
        setStupidMetadata(entity, lvl)
        ClearStupidityTask(context, this, entity, key.toString(), duration.toLong())
        if (entity is Player) {
            when (stupidity) {
                0 -> entity.chat("${ChatColor.GREEN}i am sTuPiD")  // Make sure this looks stupid
                in 1..3 -> entity.chat("${ChatColor.AQUA}i am eVen morE SDOOOOPId")  // Make sure this looks even more stupid
                in 4..6 -> entity.chat("${ChatColor.GOLD}I BEcome more stooobId than ever")  // Make sure this looks more stupider than ever
                else -> entity.chat("${ChatColor.MAGIC}I AM THE KING OF STUPIDITY")  // You get it, right?
            }
        }
    }
}