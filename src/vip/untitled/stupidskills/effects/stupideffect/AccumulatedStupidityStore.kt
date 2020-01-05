package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.helpers.PlayerStore
import java.math.BigInteger

/**
 * Stupid player tag store
 */
open class AccumulatedStupidityStore(context: JavaPlugin) : PlayerStore(context, "stupid-player-tag", "stupid_tag") {

    override fun init() {
        super.init()
        context.logger.info("${ChatColor.AQUA}[StupidEffect] Stupid player tags stored in ${table}@${dbName}")
    }

    open fun inc(player: Entity): BigInteger {
        val record = get(player)
        val accumulatedStupidity = if (record != null) {
            BigInteger(record.value, 16).add(BigInteger.ONE)
        } else {
            BigInteger.ONE
        }
        set(player, accumulatedStupidity.toString(16))
        return accumulatedStupidity
    }

    /**
     * Set accumulated stupidity
     * @param player Target player
     * @param stupidity Accumulated stupidity to set
     */
    open fun setAccumulatedStupidity(player: Entity, stupidity: BigInteger) {
        if (stupidity == BigInteger.ZERO) {
            clearAccumulatedStupidity(player)
        } else set(player, stupidity.toString(16))
    }

    /**
     * Get accumulated stupidity of a player
     * @param player Target player
     * @return Accumulated stupidity of the player
     */
    open fun getAccumulatedStupidity(player: Entity): BigInteger {
        val value = get(player)?.value
        return if (value == null) BigInteger.ZERO else BigInteger(value, 16)
    }

    /**
     * Clear accumulated stupidity of a player
     * @param player Target player
     */
    open fun clearAccumulatedStupidity(player: Entity) {
        remove(player)
    }
}