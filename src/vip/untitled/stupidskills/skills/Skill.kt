package vip.untitled.stupidskills.skills

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

/**
 * Base class for all skills
 */
@Suppress("LeakingThis")
abstract class Skill constructor(val context: JavaPlugin, val enchantment: SkillEnchantment) : Listener {
    companion object : SkillCompanionObject<Skill>(Skill::class.java) {
        override fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): Skill {
            throw Error("Cannot instantiate base class of skill")
        }

        val skills = mutableMapOf<String, Skill>()
        fun forName(skillName: String): Skill? {
            return skills[skillName]
        }
    }

    /**
     * Name of the skill
     */
    abstract val name: String
    /**
     * Internal name of the skill
     */
    abstract val internalName: String
    /**
     * Description of the skill
     */
    abstract val description: String
    /**
     * Usage of the skill
     */
    abstract val usage: String
    /**
     * Namespaced key for this skill
     */
    abstract val key: NamespacedKey

    open fun onEnable(): Skill {
        if (skills.containsKey(internalName)) {
            throw RuntimeException("Skill $internalName already registered")
        }
        context.server.pluginManager.registerEvents(this, context)
        skills[internalName] = this
        return this
    }

    /**
     * On skill disabled, unregister event listeners, etc.
     */
    open fun onDisable(): Skill {
        HandlerList.unregisterAll(this)
        skills.remove(internalName)
        return this
    }

    /**
     * Get skill item
     */
    open fun getItem(owner: Player, level: Int = 1): ItemStack {
        val sanitizedLevel = when {
            level < 1 -> 1
            level > 10 -> 10
            else -> level
        }
        val item = ItemStack(Material.WRITTEN_BOOK)
        val meta = item.itemMeta as BookMeta
        meta.author = owner.displayName
        meta.title = "[Skill] $name"
        meta.addPage(key.toString(), owner.uniqueId.toString())
        meta.generation = BookMeta.Generation.ORIGINAL
        meta.lore = mutableListOf(description, "Level $sanitizedLevel", usage)
        item.itemMeta = meta
        item.addEnchantment(enchantment, sanitizedLevel)
        return item
    }

    /**
     * Make an entity cast a skill, return true if should cancel PlayerInteractEvent
     * @param caster Caster
     * @param level Skill level
     * @param event The PlayerInteractEvent event
     */
    open fun cast(caster: Entity, level: Int, event: PlayerInteractEvent?): Boolean {
        return true
    }

    /**
     * Call `cast` when skill book is "used", override this method if you want a different behavior
     */
    @EventHandler
    open fun onPlayerUse(event: PlayerInteractEvent) {
        val level = match(event.item, event.player)
        if (level > 0) {
            if (cast(event.player, level, event)) {
                event.isCancelled = true
            }
        }
    }

    /**
     * Determine if the item and the player are matched, return the skill level
     */
    open fun match(item: ItemStack?, owner: Player): Int {
        if (item?.type == Material.WRITTEN_BOOK && (item.itemMeta as BookMeta).generation == BookMeta.Generation.ORIGINAL) {
            val level = item.enchantments[enchantment]
            if (level != null && level > 0) {
                // Enchantment matched
                val pages = (item.itemMeta as BookMeta).pages
                if (pages.size >= 2) {
                    val namespaceAndKey = pages[0].split(':')
                    if (
                        namespaceAndKey.size == 2 &&
                        namespaceAndKey[0] == key.namespace &&
                        namespaceAndKey[1] == key.key &&
                        owner.uniqueId.toString() == pages[1]
                    ) {
                        // Skill and owner matched
                        return level
                    }
                }
                return 0
            }
        }
        return 0
    }
}
