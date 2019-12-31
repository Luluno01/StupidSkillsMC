package vip.untitled.stupidskills.effects

import kotlin.math.min

abstract class EffectAdapter : Effect {
    abstract val maxLevel: Int
    open fun sanitizeLevel(level: Int): Int {
        return if (level < 1) 1 else min(level, maxLevel)
    }

    open fun getLeveledValue(startingValue: Int, bonusValue: Int, level: Int): Int {
        return startingValue + bonusValue * (level - 1)
    }

    open fun getLeveledValue(startingValue: Float, bonusValue: Float, level: Int): Float {
        return startingValue + bonusValue * (level - 1)
    }

    open fun getLeveledValue(startingValue: Double, bonusValue: Double, level: Int): Double {
        return startingValue + bonusValue * (level - 1)
    }
}