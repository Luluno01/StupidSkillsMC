package vip.untitled.stupidskills.skills

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment
import vip.untitled.stupidskills.effects.stupideffect.StupidEffect
import vip.untitled.stupidskills.helpers.LookingAt
import vip.untitled.stupidskills.helpers.SkillCooldown
import vip.untitled.stupidskills.helpers.TickCounter

open class Stupefy constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment) {
    companion object : SkillCompanionObject<Stupefy>(Stupefy::class.java)

    override val name = "Stupefy"
    override val internalName = "Stupefy"
    override val description = "Stupefy a living entity *be careful not to stupefy yourself"
    override val usage = "Right click on a living entity"
    override val key = NamespacedKey(context, "stupefy-skill")

    open class Cooldown(override var tickCounter: TickCounter) : SkillCooldown.Companion.Cooldownable() {
        override fun getCooldown(level: Int): Int {
            return 20 * (15 - level)
        }
    }

    protected open fun checkCooldown(caster: LivingEntity, level: Int, item: ItemStack? = null): Boolean {
        return Cooldown((context as TickCounter.Companion.TickCounterOwner).tickCounter)
            .checkCooldown(caster, level, item)
    }

    override fun cast(caster: Entity, level: Int, event: PlayerInteractEvent?): Boolean {
        if (caster is LivingEntity) {
            if (checkCooldown(caster, level, event?.item)) return true
            val target = LookingAt.what(caster, 3, true)
            if (target != null && target is LivingEntity) {
                val stupidEffect = StupidEffect.getInstance()
                val stupidity = stupidEffect.stupidMetadataHandler.getStupidity(target)
                val lvl = stupidEffect.sanitizeLevel(level)
                if (stupidity > lvl) {
                    // Don not override higher level
                    if (caster is Player) {
                        caster.sendActionBar("${ChatColor.AQUA}He is already more stupider than you want him to be")
                    }
                    return true
                }
                if (stupidEffect.stupidPigHandler.isStupidPig(target)) return true
                if (stupefy(target, lvl)) {
                    if (caster is Player) {
                        caster.chat("Look at this stupid ${target.name}")
                    }
                    Bukkit.broadcastMessage("${ChatColor.AQUA}${caster.name}${ChatColor.RESET} stupefied ${ChatColor.DARK_PURPLE}${target.name} ${ChatColor.GOLD}(level $lvl)")
                    if (caster is Player) {
                        caster.sendActionBar("${ChatColor.GOLD}Stupefy!")
                    }
                }
                return true
            } else {
                Bukkit.broadcastMessage("${ChatColor.AQUA}${caster.name}${ChatColor.RESET} stupefied himself/herself by accident!")
                if (stupefy(caster, 1)) {
                    if (caster is Player) {
                        caster.sendActionBar("${ChatColor.GOLD}Self-Stupefied!")
                    }
                }
            }
        }
        return true
    }

    open fun stupefy(target: LivingEntity, level: Int): Boolean {
        return StupidEffect.getInstance().apply(target, level)
    }
}