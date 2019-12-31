package vip.untitled.stupidskills.effects

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin

open class RiddenByPigEffect : Effect {
    open fun apply(entity: Entity, count: Int = 1, pigName: String? = null): MutableList<Entity> {
        val pigs = mutableListOf<Entity>()
        var bottomEntity: Entity? = null
        val world = entity.world
        for (i in 1..count) {
            val pig = world.spawnEntity(entity.location, EntityType.PIG)
            pigs.add(pig)
            if (pigName != null) pig.customName = pigName
            if (bottomEntity != null) pig.addPassenger(bottomEntity)
            bottomEntity = pig
        }
        if (bottomEntity != null) entity.addPassenger(bottomEntity)
        return pigs
    }

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity)
    }
}