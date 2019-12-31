package vip.untitled.stupidskills.effects

import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

/**
 * Shoot an entity out (change its speed and play sound effect)
 */
open class ShootEffect : EffectAdapter(), Effect {
    companion object {
        const val minSpeed = 1.0
        const val maxSpeed = 2.5
        const val maxLevel = 10
        const val extraSpeedPerLevel = (maxSpeed - minSpeed) / (maxLevel - 1)
    }

    override val maxLevel = Companion.maxLevel

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        val lvl = sanitizeLevel(level)
        entity.velocity = Vector().copy(entity.location.direction).normalize()
            .multiply(getLeveledValue(minSpeed, extraSpeedPerLevel, lvl))
        entity.world.playEffect(entity.location, org.bukkit.Effect.SMOKE, 20)
        entity.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, .3f + 0.025f * lvl, 1.7f + 0.25f * lvl)
    }
}