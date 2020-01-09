package vip.untitled.stupidskills.effects

import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin

open class PigSoundEffect : EffectAdapter() {
    companion object {
        const val maxLevel = 10
        const val minVolume = 0.5f
        const val maxVolume = 1.0f
        const val extraVolumePerLevel = (maxVolume - minVolume) / (maxLevel - 1)
    }

    override val maxLevel = Companion.maxLevel

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        val lvl = sanitizeLevel(level)
        entity.world.playSound(
            entity.location,
            Sound.ENTITY_PIG_AMBIENT,
            getLeveledValue(minVolume, extraVolumePerLevel, lvl),
            1.3f
        )
    }
}