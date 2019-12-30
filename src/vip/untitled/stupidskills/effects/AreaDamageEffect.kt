package vip.untitled.stupidskills.effects

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

open class AreaDamageEffect : Effect {
    /**
     * Apply area damage effect around `entity`, make `entity` the cause of damage
     */
    override fun apply(entity: Entity, context: Plugin, level: Int) {
        apply(entity, entity, level)
    }

    open fun getDamage(level: Int): Double {
        return 6.0 + 1.25 * level
    }

    open fun getRange(level: Int): Double {
        return 2.0 + level * 0.25
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