package vip.untitled.stupidskills.skills

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import vip.untitled.stupidskills.SkillEnchantment
import vip.untitled.stupidskills.effects.AreaDamageEffect
import vip.untitled.stupidskills.effects.ExplosionEffect
import vip.untitled.stupidskills.effects.ShootEffect
import java.util.*

class SteveCannonSkill private constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment), Listener {
    companion object : SkillCompanionObject {
        private var instance: SteveCannonSkill? = null
        override fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): SteveCannonSkill {
            if (instance == null) {
                instance = SteveCannonSkill(context, enchantment)
            }
            return instance!!
        }
    }

    override val name = "Steve Cannon"
    override val description = "Fire yourself like a cannonball"
    override val usage = "Left/Right click to fire yourself like a cannonball"
    override val key = NamespacedKey(context, "steve-cannon-skill")
    private val playerNotMoveTimeout = hashMapOf<UUID, BukkitTask>()
    private val effectivePlayerMeta = object : MetadataValueAdapter(context) {
        override fun value(): Any? {
            return true
        }

        override fun invalidate() {
            return
        }
    }

    init {
        skills["SteveCannon"] = this
        context.server.pluginManager.registerEvents(this, context)
    }

    class CastingSkillLevel(plugin: JavaPlugin, private val level: Int) : MetadataValueAdapter(plugin) {
        override fun value(): Int {
            return level
        }

        override fun invalidate() {
            return
        }
    }

    private fun getPlayerMetaOfSkill(level: Int): MetadataValueAdapter {
        return CastingSkillLevel(context, level)
    }

    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val level = match(event.item, event.player)
        if (level > 0) {
            event.isCancelled = true
            cast(event.player, level)
        }
    }

    /**
     * Make a player cast this skill
     */
    fun cast(caster: Player, level: Int) {
        if (isCaster(caster)) return
        caster.sendActionBar(ChatColor.GOLD.toString() + "Steve Cannon!")
        ShootEffect().apply(caster, context, level)
        markCaster(caster, level)
    }

    /**
     * Mark a player as a caster
     */
    private fun markCaster(caster: Player, level: Int) {
        caster.setMetadata(key.toString(), getPlayerMetaOfSkill(level))
        // Mark effective after 5 ticks to avoid immediate effect
        context.server.scheduler.runTaskLater(context, Runnable { markEffective(caster) }, 5)
        playerNotMoveTimeout[caster.uniqueId] = context.server.scheduler.runTaskLater(context, Runnable {
            // If this happens, the player does not move, apply the effect now
            if (isCaster(caster)) {
                applyEffect(caster)
            }
            playerNotMoveTimeout.remove(caster.uniqueId)
        }, 6)
    }

    private fun markEffective(caster: Player) {
        if (isCaster(caster)) {
            caster.setMetadata("$key-effective", effectivePlayerMeta)
        }
    }

    private fun unmarkCaster(caster: Player) {
        caster.removeMetadata(key.toString(), context)
    }

    private fun unmarkEffective(caster: Player) {
        caster.removeMetadata("$key-effective", context)
    }

    private fun isCaster(caster: Player): Boolean {
        val metas = caster.getMetadata(key.toString())
        for (meta in metas) {
            if (meta is CastingSkillLevel && meta.value() > 0) {
                return true
            }
        }
        return false
    }

    private fun isEffective(caster: Player): Boolean {
        val metas = caster.getMetadata("$key-effective")
        for (meta in metas) {
            if (meta.value() == true) {
                return true
            }
        }
        return false
    }

    @EventHandler
    fun onPlayerExit(event: PlayerQuitEvent) {
        val taskId = playerNotMoveTimeout.remove(event.player.uniqueId)?.taskId
        if (taskId != null) context.server.scheduler.cancelTask(taskId)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (isCaster(player) && isEffective(player)) {
            // The caster is moving, and the skill is effective on the caster
            val taskId = playerNotMoveTimeout[player.uniqueId]?.taskId
            if (taskId != null) {
                playerNotMoveTimeout.remove(player.uniqueId)
                context.server.scheduler.cancelTask(taskId)
            }
            // Check if the player hits another player, mob or the ground
            if (player.isOnGround) {
                applyEffect(player)
                return
            }
            for (entity in player.getNearbyEntities(2.0, 1.0, 2.0)) {
                if (entity is Player || entity is Mob) {
                    applyEffect(player)
                    return
                }
            }
        }
    }

    /**
     * Apply the explosion and stun effect around the player
     */
    private fun applyEffect(player: Player) {
        unmarkCaster(player)
        unmarkEffective(player)
        var level = 1
        for (meta in player.getMetadata(key.toString())) {
            if (meta is CastingSkillLevel) {
                level = meta.value()
                break
            }
        }
        AreaDamageEffect().apply(player, context, level)
        ExplosionEffect().apply(player, context, level)
    }

    override fun onDisable() {
        skills.remove("SteveCannon")
        HandlerList.unregisterAll(this)
    }
}