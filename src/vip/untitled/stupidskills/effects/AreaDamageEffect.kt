package vip.untitled.stupidskills.effects

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

open class AreaDamageEffect : Effect {
    override fun apply(entity: Entity, context: Plugin, level: Int) {
        val range = 2.0 + level * 0.25
        for (ent in entity.getNearbyEntities(range, range, range)) {
            if (ent is Player || ent is Mob) {
                (ent as Damageable).damage(6.0 + 1.25 * level, entity)
            }
        }
    }
}