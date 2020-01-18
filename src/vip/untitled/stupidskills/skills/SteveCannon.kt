package vip.untitled.stupidskills.skills

import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
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

class SteveCannon constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment), Listener {
    companion object : SkillCompanionObject<SteveCannon>(SteveCannon::class.java)

    override val name = "Steve Cannon"
    override val internalName = "SteveCannon"
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

    /**
     * Make an entity cast this skill
     */
    override fun cast(caster: Entity, level: Int, event: PlayerInteractEvent?): Boolean {
        if (isCaster(caster)) return true
        if (caster is Player) caster.sendActionBar(ChatColor.GOLD.toString() + "Steve Cannon!")
        ShootEffect().apply(caster, context, level)
        markCaster(caster, level)
        return true
    }

    /**
     * Mark a player as a caster
     */
    private fun markCaster(caster: Entity, level: Int) {
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

    private fun markEffective(caster: Entity) {
        if (isCaster(caster)) {
            caster.setMetadata("$key-effective", effectivePlayerMeta)
        }
    }

    private fun unmarkCaster(caster: Entity) {
        caster.removeMetadata(key.toString(), context)
    }

    private fun unmarkEffective(caster: Entity) {
        caster.removeMetadata("$key-effective", context)
    }

    private fun isCaster(caster: Entity): Boolean {
        val metas = caster.getMetadata(key.toString())
        for (meta in metas) {
            if (meta is CastingSkillLevel && meta.owningPlugin == context && meta.value() > 0) {
                return true
            }
        }
        return false
    }

    private fun isEffective(caster: Entity): Boolean {
        val metas = caster.getMetadata("$key-effective")
        for (meta in metas) {
            if (meta.owningPlugin == context && meta.value() == true) {
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
     * Apply the explosion and stun effect around the entity
     */
    private fun applyEffect(player: Entity) {
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
}