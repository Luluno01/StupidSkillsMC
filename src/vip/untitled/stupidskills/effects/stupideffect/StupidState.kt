package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.EffectAdapter
import vip.untitled.stupidskills.effects.stupideffect.ClearStupidStateTask.Companion.clearStupidityTasks
import vip.untitled.stupidskills.effects.stupideffect.events.EntityDiedStupidlyEvent
import vip.untitled.stupidskills.effects.stupideffect.events.EntityStupefiedEvent
import vip.untitled.stupidskills.effects.stupideffect.events.RemainingStupidDurationEvent
import vip.untitled.stupidskills.effects.stupideffect.events.StupidPlayerExitEvent
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidMetadataValue
import vip.untitled.stupidskills.helpers.Pluggable

open class StupidState(val context: JavaPlugin, val collection: StupidFeatureCollection) : EffectAdapter(), Listener,
    Pluggable {
    companion object {
        const val maxLevel = StupidEffect.maxLevel
        const val minDuration = 20 * 10
        const val maxDuration = 20 * 30
        const val extraTicksPerLevel = (maxDuration - minDuration) / (maxLevel - 1.0)
    }

    override val maxLevel = Companion.maxLevel

    open fun getDuration(level: Int): Int {
        return getLeveledValue(
            minDuration.toDouble(),
            extraTicksPerLevel, sanitizeLevel(level)
        ).toInt()
    }

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity, level, collection.stupidMetadataHandler)
    }

    open fun apply(
        entity: Entity,
        level: Int,
        stupidMetadataHandler: StupidMetadataValue.Companion.StupidMetadataHandler
    ) {
        if (entity is LivingEntity) {
            if (entity is Player) collection.store.inc(entity)
            val lvl = sanitizeLevel(level)
            val duration = getDuration(lvl)
            Bukkit.getPluginManager().callEvent(EntityStupefiedEvent(duration, lvl))
            stupidMetadataHandler.setStupidMetadata(entity, lvl)  // StupidMetadataSetEvent will be fired
            ClearStupidStateTask(
                context,
                collection,
                entity,
                getDuration(level).toLong()
            )
        }
    }

    @EventHandler
    open fun onPlayerJoin(event: PlayerJoinEvent) {
        // Check if the player has remaining ticks
        val player = event.player
        val level: Int? = collection.stupidMetadataHandler.getStupidMetadata(player)?.value()
        val remainingTicks: Int? =
            collection.remainingStupidDurationMetadataHandler.getRemainingStupidMetadata(player)?.value()
        if (level == null) {
            if (remainingTicks != null && remainingTicks > 0) {
                context.logger.warning("[StupidEffect] Player ${player.displayName} has $remainingTicks remaining tick(s), but stupid level is not specified")
            }
        } else {
            if (remainingTicks == null) {
                context.logger.warning("[StupidEffect] Player ${player.displayName} has a stupid level $level, but no remaining tick, restarting timer")
                Bukkit.getPluginManager().callEvent(
                    RemainingStupidDurationEvent(
                        player,
                        collection.stupidState.getDuration(level),
                        level
                    )
                )
                ClearStupidStateTask(
                    context,
                    collection,
                    player,
                    getDuration(level).toLong()
                )  // StupidMetadataSetEvent will be fired when cleared
            } else {
                Bukkit.getPluginManager().callEvent(
                    RemainingStupidDurationEvent(
                        player,
                        remainingTicks,
                        level
                    )
                )
                context.logger.info("[StupidEffect] Player ${player.displayName} continues being stupid for $remainingTicks tick(s)")
                ClearStupidStateTask(
                    context,
                    collection,
                    player,
                    remainingTicks.toLong()
                )  // StupidMetadataSetEvent will be fired when cleared
            }
        }
        collection.remainingStupidDurationMetadataHandler.clearRemainingStupidMetadata(player)
    }

    @EventHandler
    open fun onPlayerExit(event: PlayerQuitEvent) {
        // Fire the event and save remaining ticks
        val player = event.player
        val task = clearStupidityTasks[player.uniqueId]
        if (task != null) {
            context.logger.info("[StupidEffect] Saving remaining ticks for player ${player.displayName}")
            val remainingStupidDuration = task.remainingTicks.toInt()
            collection.remainingStupidDurationMetadataHandler.setRemainingStupidMetadata(
                player,
                remainingStupidDuration
            )
            task.cancel()
            Bukkit.getPluginManager().callEvent(StupidPlayerExitEvent(player, remainingStupidDuration))
        }
    }

    @EventHandler
    open fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val level = collection.stupidMetadataHandler.getStupidity(entity)
        if (level > 0) {
            collection.stupidMetadataHandler.clearStupidMetadata(entity)  // StupidMetadataSetEvent will be fired first
            Bukkit.getPluginManager().callEvent(EntityDiedStupidlyEvent(entity, level))
        }
        clearStupidityTasks[entity.uniqueId]?.cancel()
    }

    override fun onEnable() {
        context.server.pluginManager.registerEvents(this, context)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }
}