package vip.untitled.stupidskills.effects

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

open class AreaDamageEffect : EffectAdapter(), Effect {
    companion object {
        const val minDamage = 1.0
        const val maxDamage = 10.0
        const val maxLevel = 10
        const val extraDamagePerLevel = (maxDamage - minDamage) / (maxLevel - 1)
        const val minRange = 2.0
        const val maxRange = 5.0
        const val extraRangePerLevel = (maxRange - minRange) / (maxLevel - 1)
    }

    override val maxLevel = Companion.maxLevel

    /**
     * Apply area damage effect around `entity`, make `entity` the cause of damage
     */
    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity, entity, level)
    }

    open fun getDamage(level: Int): Double {
        return getLeveledValue(minDamage, extraDamagePerLevel, sanitizeLevel(level))
    }

    open fun getRange(level: Int): Double {
        return getLeveledValue(minRange, extraRangePerLevel, sanitizeLevel(level))
    }

    /**
     * Apply area damage effect around `entity`, make `source` the cause of damage
     */
    open fun apply(entity: Entity, source: Entity?, level: Int) {
        val range = getRange(level)
        val damage = getDamage(level)
        for (ent in entity.getNearbyEntities(range, range, range)) {
            if (ent is Player || ent is Mob) {
                (ent as Damageable).damage(damage, source)
            }
        }
    }
}