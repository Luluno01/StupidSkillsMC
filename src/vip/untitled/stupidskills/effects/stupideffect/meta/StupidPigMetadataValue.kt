package vip.untitled.stupidskills.effects.stupideffect.meta

import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin

/**
 * Stupid pig metadata, represents if a pig is a stupid pig. If it is and it
 * is riding or ridden by someone, make it invulnerable (Achieved by the
 * @see [vip.untitled.stupidskills.effects.stupideffect.RideAndRiddenByPigEffect.onEntityDamage]
 * handler)
 */
open class StupidPigMetadataValue(plugin: JavaPlugin) : MetadataValueAdapter(plugin) {
    companion object {
        open class StupidPigHandler(val context: JavaPlugin, val key: NamespacedKey) {
            /**
             * Set a pig as a stupid pig
             * @param pig Target pig
             * @param speedScalar Speed scalar
             */
            open fun setStupidPig(pig: Entity, speedScalar: Double) {
                if (pig is LivingEntity && pig.type == EntityType.PIG) {
                    pig.setMetadata(
                        "$key-pig",
                        StupidPigMetadataValue(
                            context
                        )
                    )
                    pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                        ?.addModifier(
                            AttributeModifier(
                                "stupid pig",
                                speedScalar,
                                AttributeModifier.Operation.ADD_SCALAR
                            )
                        )
                }
            }

            /**
             * Remove stupid pigs ridden by an entity
             * @param entity Target entity
             */
            protected open fun removeRiddenPigs(entity: Entity) {
                val vehicle = entity.vehicle
                if (vehicle != null && isStupidPig(vehicle)) {
                    removeRiddenPigs(vehicle)
                    vehicle.remove()
                }
            }

            /**
             * Remove stupid pigs riding an entity
             * @param entity Target entity
             */
            protected open fun removeRidingPigs(entity: Entity) {
                for (passenger in entity.passengers) {
                    if (isStupidPig(passenger)) {
                        removeRidingPigs(passenger)
                        passenger.remove()
                    }
                }
            }

            /**
             * Remove stupid pigs ridden by and riding an entity
             * @param entity Target entity
             */
            open fun removeStupidPigs(entity: Entity) {
                removeRiddenPigs(entity)
                removeRidingPigs(entity)
            }

            /**
             * Set pigs as stupid pigs
             * @param pigs Target pigs
             * @param speedScalar Speed scalar
             */
            open fun setStupidPigs(pigs: List<Entity>, speedScalar: Double) {
                for (pig in pigs) setStupidPig(pig, speedScalar)
            }

            /**
             * Check if an entity is a stupid pig
             * @param pig Target entity
             * @return True if the entity is a stupid pig
             */
            open fun isStupidPig(pig: Entity): Boolean {
                if (pig.type == EntityType.PIG) {
                    for (meta in pig.getMetadata("$key-pig")) {
                        if (meta.owningPlugin == context && meta is StupidPigMetadataValue) {
                            return meta.value()  // This should be true
                        }
                    }
                }
                return false
            }
        }
    }

    override fun value(): Boolean {
        return true
    }

    override fun invalidate() {
        return
    }
}