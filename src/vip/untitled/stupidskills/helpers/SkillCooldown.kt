package vip.untitled.stupidskills.helpers

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.math.BigInteger

class SkillCooldown(val item: ItemStack, val tickCounter: TickCounter) {
    companion object {
        val pattern = Regex("""^cooldown-until:(?<cooldownUntil>\d+)$""")

        enum class CooldownState {
            /**
             * Cooldown not set or cooled down
             */
            AVAILABLE,
            /**
             * In cooldown
             */
            COOLING_DOWN,
            /**
             * The actual time to cooldown is larger than expected cooldown
             */
            OVER_COOL
        }

        abstract class Cooldownable : TickCounter.Companion.TickCounterOwner {
            /**
             * Get cooldown ticks
             * @param level Skill level
             */
            abstract fun getCooldown(level: Int): Int

            /**
             * Send cooldown message to target entity
             * @param target Target entity, usually a player (which makes sense)
             */
            open fun sendCooldownMessage(target: Entity) {
                if (target is Player) {
                    target.sendActionBar("${ChatColor.RED}Not time yet!")
                }
            }

            /**
             * Check if a skill book is in cooldown, if available, make it to be cooling down
             * @param cooldownTarget Target of sending cooldown message
             * @param level Skill level
             * @param item Skill book item
             */
            open fun checkCooldown(cooldownTarget: Entity, level: Int, item: ItemStack? = null): Boolean {
                if (item != null) {
                    val cooldown = getCooldown(level)
                    val skillCooldown = SkillCooldown(item, tickCounter)
                    when (skillCooldown.getCooldownState(cooldown)) {
                        CooldownState.COOLING_DOWN -> {
                            skillCooldown.getCooldownUntil { tick, _ ->
                                if (
                                    tick == null ||
                                    /* Remaining cooldown is greater than 10 ticks */
                                    cooldown - (tick - tickCounter.tick).toInt() > 10
                                ) {
                                    sendCooldownMessage(cooldownTarget)
                                }
                            }
                            return true
                        }
                        CooldownState.OVER_COOL -> skillCooldown.removeCooldown()
                        else -> {
                        }
                    }
                    skillCooldown.apply(cooldown)
                }
                return false
            }
        }
    }

    init {
        assert(item.type == Material.WRITTEN_BOOK) { "Only written skill book is supported" }
    }

    fun apply(cooldown: Int) {
        val cooldownUntilString = "cooldown-until:${tickCounter.tick + cooldown.toBigInteger()}"
        getCooldownUntil { tick, pageNum ->
            val meta = item.itemMeta as BookMeta
            if (tick == null) meta.addPage(cooldownUntilString)
            else meta.setPage(pageNum, cooldownUntilString)
            item.itemMeta = meta
        }
    }

    fun getCooldownUntil(callback: (tick: BigInteger?, pageNum: Int) -> Unit) {
        val meta = item.itemMeta as BookMeta
        var pageNum = 0
        for (page in meta.pages) {
            pageNum++
            val cooldownUtil = pattern.matchEntire(page)?.groups?.get("cooldownUntil")?.value
            if (cooldownUtil != null) {
                callback(BigInteger(cooldownUtil), pageNum)
                return
            }
        }
        callback(null, pageNum + 1)
        return
    }

    fun removeCooldown() {
        getCooldownUntil { tick, pageNum ->
            if (tick != null) {
                val meta = item.itemMeta as BookMeta
                val pages = meta.pages.toMutableList()
                pages.removeAt(pageNum - 1)
                meta.pages = pages
            }
        }
    }

    fun getCooldownState(cooldown: Int): CooldownState {
        val cd = cooldown.toBigInteger()
        var ret = CooldownState.AVAILABLE
        getCooldownUntil { tick, _ ->
            if (tick != null && tick > tickCounter.tick) {
                ret = if (tick - tickCounter.tick <= cd) CooldownState.COOLING_DOWN else CooldownState.OVER_COOL
            }
        }
        return ret
    }
}