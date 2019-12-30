package vip.untitled.stupidskills.effects

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

open class FullAreaDamageEffect : AreaDamageEffect() {
    override fun apply(entity: Entity, context: Plugin, level: Int) {
        apply(entity, entity, level)
    }

    override fun apply(entity: Entity, source: Entity?, level: Int) {
        super.apply(entity, source, level)
        if (entity is Damageable) entity.damage(getDamage(level), source)
    }
}