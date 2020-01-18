package vip.untitled.stupidskills.skills

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import vip.untitled.stupidskills.SkillEnchantment

open class JavelinMissile constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    HomingMissile(context, enchantment) {
    companion object : SkillCompanionObject<JavelinMissile>(JavelinMissile::class.java)

    override val name = "Javelin Missile"
    override val internalName = "JavelinMissile"
    override val description = "Launch a Javelin Missile"
    override val key = NamespacedKey(context, "javelin-missile-skill")

    override fun sendMessage(entity: Entity) {
        if (entity is Player) {
            entity.sendActionBar(ChatColor.GOLD.toString() + "Javelin Missile launched!")
        }
    }

    protected open class NaiveTargetChangingTask(plugin: JavaPlugin, private val homingMissileTask: HomingMissileTask) :
        BukkitRunnable() {
        init {
            val arrow = homingMissileTask.arrow
            homingMissileTask.targetOverride =
                arrow.velocity.clone().add(arrow.location.toVector()).add(Vector(0.0, 100.0, 0.0))
            this.runTaskLater(plugin, 15)
        }

        override fun run() {
            if (!homingMissileTask.isCancelled) homingMissileTask.targetOverride = null
        }
    }

    protected open class AssKickingTargetChangingTask(
        plugin: JavaPlugin,
        private val homingMissileTask: HomingMissileTask
    ) :
        BukkitRunnable() {
        protected var gainEnoughAltitude = 0

        init {
            this.runTaskTimer(plugin, /* Equals to homing delay */ 5, /* Update overriding target every 2 ticks */ 1L)
        }

        override fun run() {
            if (homingMissileTask.isCancelled) {
                cancel()
            } else {
                val arrow = homingMissileTask.arrow
                if (gainEnoughAltitude < 10) {
                    // Gain some altitude
                    homingMissileTask.targetOverride =
                        arrow.velocity.clone().add(arrow.location.toVector()).add(Vector(0.0, 100.0, 0.0))
                    gainEnoughAltitude += 2
                    return
                } else {
                    // Then to the above
                    val targetVector = arrow.location.toVector().add(homingMissileTask.getToTargetVector())
                    val horizontalDistance = targetVector.clone().setY(0).distance(arrow.location.toVector().setY(0))
                    if (horizontalDistance < 8) {
                        // Close enough
                        homingMissileTask.targetOverride = null
                        cancel()
                    } else {
                        homingMissileTask.targetOverride = targetVector.clone().add(Vector(0.0, 50.0, 0.0))
                    }
                }
            }
        }
    }

    override fun getHomingDelay(level: Int): Long {
        return 5
    }

    override fun onNewMissile(missile: Arrow, homingMissileTask: HomingMissileTask) {
        AssKickingTargetChangingTask(context, homingMissileTask)
    }
}