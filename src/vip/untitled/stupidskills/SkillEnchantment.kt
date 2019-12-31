package vip.untitled.stupidskills

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.inventory.ItemStack

open class SkillEnchantment(key: NamespacedKey) : Enchantment(key) {
    open var registered = false
    override fun canEnchantItem(item: ItemStack): Boolean {
        return item.type == Material.WRITTEN_BOOK
    }

    override fun getItemTarget(): EnchantmentTarget {
        return EnchantmentTarget.ALL
    }

    override fun getName(): String {
        return "Skill Enchantment"
    }

    override fun isCursed(): Boolean {
        return true
    }

    override fun isTreasure(): Boolean {
        return true
    }

    override fun getMaxLevel(): Int {
        return 10
    }

    override fun getStartLevel(): Int {
        return 1
    }

    override fun conflictsWith(other: Enchantment): Boolean {
        return other is SkillEnchantment
    }

    open fun registerSelf(): SkillEnchantment {
        if (registered) return this
        val field = Enchantment::class.java.getDeclaredField("acceptingNew")
        field.isAccessible = true
        field.set(null, true)
        registerEnchantment(this)
        field.set(null, false)
        field.isAccessible = false
        registered = true
        return this
    }
}