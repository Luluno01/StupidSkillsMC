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
    companion object : SkillCompanionObject<JavelinMissile>(JavelinMissile::class)

    override val name = "Javelin Missile"
    override val internalName = "JavelinMissile"
    override val description = "Launch a Javelin Missile"
    override val key = NamespacedKey(context, "javelin-missile-skill")

    override fun sendMessage(entity: Entity) {
        if (entity is Player) {
            entity.sendActionBar(ChatColor.GOLD.toString() + "Javelin Missile launched!")
        }
    }

    protected open class TargetChangingTask(plugin: JavaPlugin, private val homingMissileTask: HomingMissileTask) :
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

    override fun onNewMissile(missile: Arrow, homingMissileTask: HomingMissileTask) {
        TargetChangingTask(context, homingMissileTask)
    }
}