package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin

/**
 * Explosion effect (visual and sound effect only)
 */
open class ExplosionEffect : Effect {
    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        entity.world.createExplosion(entity, 0f, false, false)
    }
}