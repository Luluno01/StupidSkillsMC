package vip.untitled.stupidskills.skills

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment
import vip.untitled.stupidskills.effects.KnockbackEffect
import vip.untitled.stupidskills.effects.PigSoundEffect

open class Piggy(
    context: JavaPlugin, enchantment: SkillEnchantment
) : Skill(context, enchantment) {
    companion object : SkillCompanionObject<Piggy>(Piggy::class)

    override val name = "Piggy Piggy"
    override val internalName = "Piggy"
    override val description = "Play with piggy"
    override val usage = "Right click on your target"
    override val key = NamespacedKey(context, "piggy-skill")

    override fun cast(caster: Entity, level: Int, event: PlayerInteractEvent?): Boolean {
        return true
    }

    open fun cast(caster: Entity, target: Entity, level: Int, event: PlayerInteractEntityEvent?): Boolean {
        if (target is LivingEntity) {
            KnockbackEffect().apply(caster, target, context, level)
            PigSoundEffect().apply(target, context, level)
        }
        return true
    }

    @EventHandler
    open fun onPlayerUse(event: PlayerInteractEntityEvent) {
        val player = event.player
        val level = match(player.inventory.itemInMainHand, player)
        if (level > 0) {
            if (cast(player, event.rightClicked, level, event)) event.isCancelled = true
        }
    }
}