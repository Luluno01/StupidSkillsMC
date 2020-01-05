package vip.untitled.stupidskills.effects.stupideffect.meta

import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.stupideffect.StupidFeatureCollection

/**
 * Remaining stupid duration metadata, represents the remaining duration of
 * being stupid of an offline player
 * The player should continue being stupid for that amount of ticks when next
 * login
 */
open class RemainingStupidDurationMetadataValue(plugin: JavaPlugin, private val duration: Int) :
    MetadataValueAdapter(plugin) {
    companion object {
        open class RemainingStupidDurationMetadataHandler(
            val context: JavaPlugin,
            val key: NamespacedKey,
            val collection: StupidFeatureCollection
        ) {
            /**
             * Set remaining stupid duration metadata of an entity
             * @param entity Target entity
             * @param duration Remaining ticks
             */
            open fun setRemainingStupidMetadata(entity: LivingEntity, duration: Int) {
                entity.setMetadata(
                    "$key-remaining",
                    RemainingStupidDurationMetadataValue(
                        context,
                        duration
                    )
                )
            }

            /**
             * Get remaining stupid duration metadata of an entity
             * @param entity Target entity
             * @return Remaining stupid duration metadata, null if not set
             */
            open fun getRemainingStupidMetadata(entity: LivingEntity): RemainingStupidDurationMetadataValue? {
                for (meta in entity.getMetadata("$key-remaining")) {
                    if (meta.owningPlugin == context && meta is RemainingStupidDurationMetadataValue) return meta
                }
                return null
            }

            /**
             * Clear remaining stupid duration metadata of an entity
             * @param entity Target entity
             */
            open fun clearRemainingStupidMetadata(entity: LivingEntity) {
                entity.removeMetadata("$key-remaining", context)
            }
        }
    }

    override fun value(): Int {
        return duration
    }

    override fun invalidate() {
        return
    }
}