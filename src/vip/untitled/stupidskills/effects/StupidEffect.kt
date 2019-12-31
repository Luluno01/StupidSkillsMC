package vip.untitled.stupidskills.effects

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.vehicle.VehicleExitEvent
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
        const val minSpeedScaler = 1.1
        const val maxSpeedScaler = 1.5
        const val extraSpeedScalerPerLevel = (maxSpeedScaler - minSpeedScaler) / (maxLevel - 1.0)
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

    open class StupidPigMetadataValue(plugin: JavaPlugin) : MetadataValueAdapter(plugin) {
        override fun value(): Boolean {
            return true
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
//            if (entity.isDead) return  // Dead for being stupid?
            effect.clearStupidMetadata(entity)
            effect.removeStupidPigs(entity)
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

    open fun setStupidPig(pig: Entity, level: Int) {
        if (pig is LivingEntity && pig.type == EntityType.PIG) {
            pig.setMetadata("$key-pig", StupidPigMetadataValue(context))
            pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                ?.addModifier(
                    AttributeModifier(
                        "stupid pig",
                        getLeveledValue(minSpeedScaler, extraSpeedScalerPerLevel, sanitizeLevel(level)),
                        AttributeModifier.Operation.ADD_SCALAR
                    )
                )
        }
    }

    protected open fun removeRiddenPigs(entity: Entity) {
        val vehicle = entity.vehicle
        if (vehicle != null && isStupidPig(vehicle)) {
            removeRiddenPigs(vehicle)
            vehicle.remove()
        }
    }

    protected open fun removeRidingPigs(entity: Entity) {
        for (passenger in entity.passengers) {
            if (isStupidPig(passenger)) {
                removeRidingPigs(passenger)
                passenger.remove()
            }
        }
    }

    open fun removeStupidPigs(entity: Entity) {
        removeRiddenPigs(entity)
        removeRidingPigs(entity)
    }

    open fun setStupidPigs(pigs: List<Entity>, level: Int) {
        for (pig in pigs) setStupidPig(pig, level)
    }

    open fun isStupidPig(pig: Entity): Boolean {
        if (pig.type == EntityType.PIG) {
            for (meta in pig.getMetadata("$key-pig")) {
                if (meta.owningPlugin == context && meta is StupidPigMetadataValue) {
                    return meta.value()  // This should be true
                }
            }
        }
        return false
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
                applyCompositeEffect(player, level)
                ClearStupidityTask(context, this, player, getDuration(level).toLong())

            } else {
                context.logger.info("[StupidEffect] Player ${player.displayName} continues being stupid for $remainingTicks ticks")
                clearRemainingStupidMetadata(player)
                applyCompositeEffect(player, level)
                ClearStupidityTask(context, this, player, remainingTicks.toLong())
            }
        }
    }

    @EventHandler
    open fun onEntityDeath(event: EntityDeathEvent) {
        // Cancel the timer and remove stupid metadata & pigs
        val entity = event.entity
        removeStupidPigs(entity)
        clearStupidityTasks[entity.uniqueId]?.cancel()
        clearStupidMetadata(entity)
    }

    @EventHandler
    open fun onPlayerExit(event: PlayerQuitEvent) {
        // Cancel the timer and save remaining ticks
        val player = event.player
        val task = clearStupidityTasks[player.uniqueId]
        if (task != null) {
            setRemainingStupidMetadata(player, task.remainingTicks.toInt())
            task.cancel()
            removeStupidPigs(player)
            context.logger.info("[StupidEffect] Saving remaining ticks for player ${player.displayName}")
        }
    }

    @EventHandler
    open fun onVehicleExit(event: VehicleExitEvent) {
        if (isStupidPig(event.vehicle) && getStupidity(event.exited) > 0) {
            event.isCancelled = true
        }
    }

    @EventHandler
    open fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (isStupidPig(entity) && (entity.passengers.isNotEmpty() || entity.isInsideVehicle)) event.isCancelled = true
    }

    open fun getDuration(level: Int): Int {
        return getLeveledValue(minDuration.toDouble(), extraTicksPerLevel, sanitizeLevel(level)).toInt()
    }

    open fun getStupidity(entity: LivingEntity): Int {
        val meta = getStupidMetadata(entity)
        return meta?.value() ?: 0
    }

    open fun applyCompositeEffect(entity: Entity, level: Int) {
        val lvl = sanitizeLevel(level)
        val pigs = RiddenByPigEffect().apply(entity, kotlin.math.ceil(lvl * 0.5).toInt())
        pigs.addAll(RidePigEffect().apply(entity, pigName = entity.name))
        setStupidPigs(pigs, lvl)
    }

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity, level)
    }

    /**
     * Apply stupid effect on an entity
     * @param entity Target entity
     * @param level Stupid level
     * @return Whether the effect is successfully applied
     */
    open fun apply(entity: Entity, level: Int): Boolean {
        if (entity !is LivingEntity) return false
        if (isStupidPig(entity)) return false
        val stupidity = getStupidity(entity)
        val lvl = sanitizeLevel(level)
        if (stupidity > lvl) return false  // Don not override higher level
        val duration = getDuration(lvl)
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING,
                duration,
                1,
                true,
                true,
                true
            ), true
        )
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOW,
                duration,
                1,
                true,
                true,
                true
            ), true
        )
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.WEAKNESS,
                duration,
                1,
                true,
                true,
                true
            ), true
        )
        if (stupidity == 0) applyCompositeEffect(entity, lvl)
        setStupidMetadata(entity, lvl)
        ClearStupidityTask(context, this, entity, duration.toLong())  // Start/Restart timer
        if (entity is Player) {
            when (stupidity) {
                0 -> entity.chat("${ChatColor.GREEN}i am sTuPiD")  // Make sure this looks stupid
                in 1..3 -> entity.chat("${ChatColor.AQUA}i am eVen morE SDOOOOPId")  // Make sure this looks even more stupid
                in 4..6 -> entity.chat("${ChatColor.GOLD}I BEcome more stooobId than ever")  // Make sure this looks more stupider than ever
                else -> entity.chat("${ChatColor.MAGIC}I AM THE KING OF STUPIDITY")  // You get it, right?
            }
        }
        return true
    }
}