package vip.untitled.stupidskills.effects.stupideffect.meta

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.stupideffect.events.StupidMetadataSetEvent

/**
 * Stupid metadata value, represents current stupid level (current stupidity),
 * i.e., the level of the skill, Stupefy
 * (@see [vip.untitled.stupidskills.skills.Stupefy]), that was applied on the
 * target entity
 */
open class StupidMetadataValue(plugin: JavaPlugin, val level: Int) : MetadataValueAdapter(plugin) {
    companion object {
        open class StupidMetadataHandler(val context: JavaPlugin, val key: NamespacedKey) {
            /**
             * Set stupid metadata for an entity
             * @param entity Target entity
             * @param level Stupidity level
             */
            open fun setStupidMetadata(entity: LivingEntity, level: Int) {
                entity.setMetadata(
                    key.toString(),
                    StupidMetadataValue(
                        context,
                        level
                    )
                )
                Bukkit.getPluginManager().callEvent(StupidMetadataSetEvent(entity, level))
            }

            /**
             * Get stupid metadata of an entity
             * @param entity Target entity
             * @return Stupid metadata (null if not set)
             */
            open fun getStupidMetadata(entity: LivingEntity): StupidMetadataValue? {
                for (meta in entity.getMetadata(key.toString())) {
                    if (meta.owningPlugin == context && meta is StupidMetadataValue) return meta
                }
                return null
            }

            /**
             * Clear stupid metadata for an entity
             * @param entity Target entity
             */
            open fun clearStupidMetadata(entity: LivingEntity) {
                entity.removeMetadata(key.toString(), context)
                Bukkit.getPluginManager().callEvent(StupidMetadataSetEvent(entity, 0))
            }

            /**
             * Get current stupidity level of an entity
             * @param entity Target entity
             * @return Current stupidity level of the entity
             */
            open fun getStupidity(entity: LivingEntity): Int {
                val meta = getStupidMetadata(entity)
                return meta?.value() ?: 0
            }
        }
    }

    override fun value(): Int {
        return level
    }

    override fun invalidate() {
        return
    }
}