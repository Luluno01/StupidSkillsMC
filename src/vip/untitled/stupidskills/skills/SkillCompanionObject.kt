package vip.untitled.stupidskills.skills

import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

interface SkillCompanionObject {
    fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): Skill
}