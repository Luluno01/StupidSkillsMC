package vip.untitled.stupidskills.skills

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment
import vip.untitled.stupidskills.effects.StupidEffect
import vip.untitled.stupidskills.helpers.LookingAt
import vip.untitled.stupidskills.helpers.SkillCooldown
import vip.untitled.stupidskills.helpers.TickCounter

open class Stupefy constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment) {
    companion object : SkillCompanionObject<Stupefy>(Stupefy::class)

    override val name = "Stupefy"
    override val internalName = "Stupefy"
    override val description = "Stupefy a living entity *be careful not to stupefy yourself"
    override val usage = "Right click on a living entity"
    override val key = NamespacedKey(context, "stupefy-skill")
//    protected val clearStupidityTasks = hashMapOf<UUID, ClearStupidityTask>()
//
//    open class StupidMetadataValue(plugin: JavaPlugin, val level: Int): MetadataValueAdapter(plugin) {
//        override fun value(): Int {
//            return level
//        }
//
//        override fun invalidate() {
//            return
//        }
//    }
//
//    open class RemainingStupidDurationMetadataValue(plugin: JavaPlugin, private val duration: Int): MetadataValueAdapter(plugin) {
//        override fun value(): Int {
//            return duration
//        }
//
//        override fun invalidate() {
//            return
//        }
//    }

    open class Cooldown(override var tickCounter: TickCounter) : SkillCooldown.Companion.Cooldownable() {
        override fun getCooldown(level: Int): Int {
            return 20 * (15 - level)
        }
    }

//    @Suppress("LeakingThis")
//    open class ClearStupidityTask(val plugin: JavaPlugin, val skill: Stupefy, val entity: LivingEntity, private val metaKey: String, val duration: Long): BukkitRunnable() {
//        open val targetTick = (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick + BigInteger.valueOf(duration)
//        init {
//            skill.clearStupidityTasks[entity.uniqueId]?.cancel()
//            skill.clearStupidityTasks[entity.uniqueId] = this
//            this.runTaskLater(plugin, duration)
//        }
//
//        open val remainingTicks: Long
//        get() {
//            val ticks = targetTick - (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick
//            return if(ticks.compareTo(BigInteger.ZERO) == -1) 0
//            else ticks.toLong()
//        }
//
//        override fun run() {
//            skill.clearStupidityTasks.remove(entity.uniqueId)
//            if(entity.isDead) return  // Dead for being stupid?
//            entity.removeMetadata(metaKey, plugin)
//        }
//
//        override fun cancel() {
//            skill.clearStupidityTasks.remove(entity.uniqueId)
//            super.cancel()
//        }
//    }
//
//    open fun getRemainingStupidMetadata(entity: LivingEntity): MetadataValue? {
//        for(meta in entity.getMetadata(key.toString())) {
//            if(meta.owningPlugin == context && meta is StupidMetadataValue || meta is RemainingStupidDurationMetadataValue) return meta
//        }
//        return null
//    }
//
//    @EventHandler
//    open fun onPlayerJoin(event: PlayerJoinEvent) {
//        // Check if the player has remaining ticks
//        val player = event.player
//        when (val meta = getRemainingStupidMetadata(player)) {
//            is StupidMetadataValue -> {
//                context.logger.warning("Oops, unexpected metadata value, RemainingStupidDurationMetadataValue expected, restarting timer for player ${player.displayName}")
//                ClearStupidityTask(context, this, player, key.toString(), getDuration(meta.value()).toLong())
//            }
//            is RemainingStupidDurationMetadataValue -> {
////                context.logger.info("Player ${player.displayName} continues being stupid for ${meta.value()} ticks")
//                ClearStupidityTask(context, this, player, key.toString(), meta.value().toLong())
//            }
//            null -> {}
//            else -> {
//                context.logger.warning("Unrecognized metadata value")
//            }
//        }
//    }
//
//    @EventHandler
//    open fun onPlayerExit(event: PlayerQuitEvent) {
//        // Cancel the timer and save remaining ticks
//        val player = event.player
//        val task = clearStupidityTasks[player.uniqueId]
//        if(task != null) {
//            player.setMetadata(key.toString(), RemainingStupidDurationMetadataValue(context, task.remainingTicks.toInt()))
//            task.cancel()
//            clearStupidityTasks.remove(player.uniqueId)
////            context.logger.info("Saving remaining ticks for player ${player.displayName}")
//        }
//    }
//
//    open fun getDuration(level: Int): Int {
//        return 20 * (60 + 10 * level)
//    }

    protected open fun checkCooldown(caster: LivingEntity, level: Int, item: ItemStack? = null): Boolean {
        return Cooldown((context as TickCounter.Companion.TickCounterOwner).tickCounter)
            .checkCooldown(caster, level, item)
    }

    override fun cast(caster: Entity, level: Int, event: PlayerInteractEvent?): Boolean {
        if (caster is LivingEntity) {
            if (checkCooldown(caster, level, event?.item)) return true
            val target = LookingAt.what(caster, 3, true)
            if (target != null && target is LivingEntity) {
                stupefy(target, level)
                if (caster is Player) {
                    caster.chat("Look at this stupid ${target.name}")
                }
                Bukkit.broadcastMessage("${ChatColor.AQUA}${caster.name}${ChatColor.RESET} stupefied ${ChatColor.DARK_PURPLE}${target.name} ${ChatColor.GOLD}(level $level)")
                if (caster is Player) {
                    caster.sendActionBar("${ChatColor.GOLD}Stupefy!")
                }
                return true
            } else {
                Bukkit.broadcastMessage("${ChatColor.AQUA}${caster.name}${ChatColor.RESET} stupefied himself/herself by accident!")
                stupefy(caster, 1)
                if (caster is Player) {
                    caster.sendActionBar("${ChatColor.GOLD}Self-Stupefied!")
                }
            }
        }
        return true
    }

    open fun stupefy(target: LivingEntity, level: Int) {
        StupidEffect.getInstance().apply(target, context, level)
//        val stupidity = getStupidity(target)
//        if(stupidity > level) return  // Don not override higher level
//        val duration = getDuration(level)
//        target.addPotionEffect(PotionEffect(
//            PotionEffectType.GLOWING,
//            duration,
//            level,
//            true,
//            true,
//            true
//        ), true)
//        target.addPotionEffect(PotionEffect(
//            PotionEffectType.SLOW,
//            duration,
//            level,
//            true,
//            true,
//            true
//        ), true)
//        target.addPotionEffect(PotionEffect(
//            PotionEffectType.WEAKNESS,
//            duration,
//            level,
//            true,
//            true,
//            true
//        ), true)
//        target.setMetadata(key.toString(), StupidMetadataValue(context, level))
//        ClearStupidityTask(context, this, target, key.toString(), duration.toLong())
//        if(target is Player) {
//            when(stupidity) {
//                0 -> target.chat("${ChatColor.GREEN}i am sTuPiD")  // Make sure this looks stupid
//                1 -> target.chat("${ChatColor.AQUA}i am eVen morE SDOOOOPId")  // Make sure this looks even more stupid
//                2 -> target.chat("${ChatColor.GOLD}I BEcome more stooobId than ever")  // Make sure this looks more stupider than ever
//                else -> target.chat("${ChatColor.MAGIC}I AM THE KING OF STUPIDITY")  // You get it, right?
//            }
//        }
    }

//    open fun getStupidity(entity: LivingEntity): Int {
//        for(meta in entity.getMetadata(key.toString())) {
//            if(meta is StupidMetadataValue && meta.owningPlugin == context) {
//                return meta.value()
//            }
//        }
//        return 0
//    }
}