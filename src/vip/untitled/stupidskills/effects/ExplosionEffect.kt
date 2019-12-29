package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

/**
 * Explosion effect (visual and sound effect only)
 */
open class ExplosionEffect : Effect {
    override fun apply(entity: Entity, context: Plugin, level: Int) {
        entity.world.createExplosion(entity, 0f, false, false)
    }
}