package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.entity.LivingEntity
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import vip.untitled.stupidskills.helpers.TickCounter
import java.math.BigInteger
import java.util.*

@Suppress("LeakingThis")
open class ClearStupidStateTask(
    val plugin: JavaPlugin,
    val collection: StupidFeatureCollection,
    val entity: LivingEntity,
    val duration: Long
) : BukkitRunnable() {
    companion object {
        val clearStupidityTasks = hashMapOf<UUID, ClearStupidStateTask>()
    }

    open val targetTick =
        (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick + BigInteger.valueOf(
            duration
        )

    init {
        clearStupidityTasks[entity.uniqueId]?.cancel()
        clearStupidityTasks[entity.uniqueId] = this
        this.runTaskLater(plugin, duration)
    }

    open val remainingTicks: Long
        get() {
            val ticks = targetTick - (plugin as TickCounter.Companion.TickCounterOwner).tickCounter.tick
            return if (ticks.compareTo(BigInteger.ZERO) == -1) 0
            else ticks.toLong()
        }

    override fun run() {
        clearStupidityTasks.remove(entity.uniqueId)
//            if (entity.isDead) return  // Dead for being stupid?
        collection.stupidMetadataHandler.clearStupidMetadata(entity)
    }

    override fun cancel() {
        clearStupidityTasks.remove(entity.uniqueId)
        super.cancel()
    }
}