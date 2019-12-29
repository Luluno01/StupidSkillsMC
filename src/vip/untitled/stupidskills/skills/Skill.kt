package vip.untitled.stupidskills.skills

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

/**
 * Base class for all skills
 */
abstract class Skill protected constructor(val context: JavaPlugin, val enchantment: SkillEnchantment) {
    companion object : SkillCompanionObject {
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

    /**
     * On skill disabled, unregister event listeners, etc.
     */
    abstract fun onDisable()

    /**
     * Get skill item
     */
    open fun getItem(owner: Player, level: Int = 1): ItemStack {
        val sanitizedLevel = when {
            level < 1 -> 1
            level > 3 -> 3
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
                return level
            }
        }
        return 0
    }
}
