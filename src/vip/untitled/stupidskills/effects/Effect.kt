package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin

/**
 * Applicable skill effect
 */
interface Effect {
    fun apply(entity: Entity, context: JavaPlugin, level: Int = 1)
}