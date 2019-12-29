package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

/**
 * Applicable skill effect
 */
interface Effect {
    fun apply(entity: Entity, context: Plugin, level: Int = 1)
}