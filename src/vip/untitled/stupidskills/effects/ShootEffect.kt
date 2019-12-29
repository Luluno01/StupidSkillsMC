package vip.untitled.stupidskills.effects

import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * Shoot an entity out (change its speed and play sound effect)
 */
open class ShootEffect : Effect {
    override fun apply(entity: Entity, context: Plugin, level: Int) {
        entity.velocity = Vector().copy(entity.location.direction).normalize().multiply(1.4 + 0.2 * level)
        entity.world.playEffect(entity.location, org.bukkit.Effect.SMOKE, 20)
        entity.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, .3f + 0.05f * level, 1.7f + 0.5f * level)
    }
}