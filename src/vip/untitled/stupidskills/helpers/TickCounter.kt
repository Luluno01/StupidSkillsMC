package vip.untitled.stupidskills.helpers

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.math.BigInteger

class TickCounter(plugin: JavaPlugin) : BukkitRunnable() {
    companion object {
        interface TickCounterOwner {
            var tickCounter: TickCounter
        }
    }

    var tick: BigInteger = BigInteger.ZERO

    init {
        runTaskTimer(plugin, 0L, 0L)
    }

    override fun run() {
        tick++
    }
}