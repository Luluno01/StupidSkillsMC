package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.Effect
import vip.untitled.stupidskills.effects.EffectAdapter
import vip.untitled.stupidskills.effects.RiddenByPigEffect
import vip.untitled.stupidskills.effects.RidePigEffect
import vip.untitled.stupidskills.effects.stupideffect.events.RemainingStupidDurationEvent
import vip.untitled.stupidskills.effects.stupideffect.events.StupidMetadataSetEvent
import vip.untitled.stupidskills.effects.stupideffect.events.StupidPlayerExitEvent
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidPigMetadataValue
import vip.untitled.stupidskills.helpers.Pluggable

open class RideAndRiddenByPigEffect(
    protected val context: JavaPlugin,
    protected val collection: StupidFeatureCollection
) : Effect, EffectAdapter(), Pluggable, Listener {
    companion object {
        const val maxLevel = StupidEffect.maxLevel
        const val minSpeedScalar = 1.1
        const val maxSpeedScalar = 1.5
        const val extraSpeedScalarPerLevel = (maxSpeedScalar - minSpeedScalar) / (maxLevel - 1.0)
    }

    override val maxLevel =
        Companion.maxLevel

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity, level, collection.stupidPigHandler)
    }

    open fun apply(entity: Entity, level: Int, stupidPigHandler: StupidPigMetadataValue.Companion.StupidPigHandler?) {
        val lvl = sanitizeLevel(level)
        val pigs = RiddenByPigEffect().apply(entity, kotlin.math.ceil(lvl * 0.5).toInt())
        pigs.addAll(RidePigEffect().apply(entity, pigName = entity.name))
        stupidPigHandler?.setStupidPigs(
            pigs, getLeveledValue(
                minSpeedScalar,
                extraSpeedScalarPerLevel,
                sanitizeLevel(level)
            )
        )
    }

    @EventHandler
    open fun onStupidMetadataSet(event: StupidMetadataSetEvent) {
        val entity = event.entity
        val level = event.level
        val stupidPigHandler = collection.stupidPigHandler
        stupidPigHandler.removeStupidPigs(entity)
        if (level > 0) {
            apply(entity, level, stupidPigHandler)
        }
    }

    @EventHandler
    open fun onRemainingStupidDuration(event: RemainingStupidDurationEvent) {
        apply(event.player, event.level, collection.stupidPigHandler)
    }

    @EventHandler
    open fun onStupidPlayerExit(event: StupidPlayerExitEvent) {
        collection.stupidPigHandler.removeStupidPigs(event.player)
    }

    @EventHandler
    open fun onEntityDeath(event: EntityDeathEvent) {
        // Prevent drops
        val entity = event.entity
        if (collection.stupidPigHandler.isStupidPig(entity)) {
            event.droppedExp = 0
            event.drops.clear()
        }
    }

    @EventHandler
    open fun onVehicleExit(event: VehicleExitEvent) {
        if (collection.stupidPigHandler.isStupidPig(event.vehicle) && collection.stupidMetadataHandler.getStupidity(
                event.exited
            ) > 0
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler
    open fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (collection.stupidPigHandler.isStupidPig(entity) && (entity.passengers.isNotEmpty() || entity.isInsideVehicle)) event.isCancelled =
            true
    }

    override fun onEnable() {
        context.server.pluginManager.registerEvents(this, context)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }
}