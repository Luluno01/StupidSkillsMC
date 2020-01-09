package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.plugin.java.JavaPlugin

open class KnockbackEffect : EffectAdapter() {
    companion object {
        const val maxLevel = 10
        const val minSpeed = 1.0
        const val maxSpeed = 2.0
        const val extraSpeedPerLevel = (maxSpeed - minSpeed) / (maxLevel - 1)
    }

    override val maxLevel = Companion.maxLevel

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        throw RuntimeException("Use the overloaded method instead")
    }

    open fun apply(from: Entity, to: LivingEntity, context: JavaPlugin, level: Int) {
        val lvl = sanitizeLevel(level)
        val speed = getLeveledValue(
            minSpeed,
            extraSpeedPerLevel,
            lvl
        )
        var velocity = to.location.toVector().subtract(from.location.toVector())
            .normalize().multiply(speed).setY(0.5)
        if (velocity.length() == 0.0) {
            velocity = to.location.direction.normalize().multiply(-speed).setY(0.5)
        }
        to.velocity = velocity
    }
}