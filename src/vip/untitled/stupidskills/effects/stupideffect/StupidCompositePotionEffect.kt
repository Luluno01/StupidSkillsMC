package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import vip.untitled.stupidskills.effects.Effect
import vip.untitled.stupidskills.effects.stupideffect.events.StupidMetadataSetEvent
import vip.untitled.stupidskills.helpers.Pluggable

open class StupidCompositePotionEffect(
    protected val context: JavaPlugin,
    protected val collection: StupidFeatureCollection
) : Effect, Pluggable, Listener {
    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        if (entity !is LivingEntity) return
        apply(entity, collection.stupidState.getDuration(level))
    }

    open fun apply(entity: LivingEntity, duration: Int) {
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
    }

    @EventHandler
    open fun onStupidMetadataSet(event: StupidMetadataSetEvent) {
        val entity = event.entity
        val level = event.level
        if (level > 0) {
            apply(entity, collection.stupidState.getDuration(level))
        }
    }

    override fun onEnable() {
        context.server.pluginManager.registerEvents(this, context)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }
}