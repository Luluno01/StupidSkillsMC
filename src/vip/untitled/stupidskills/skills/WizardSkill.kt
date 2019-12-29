package vip.untitled.stupidskills.skills

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

class WizardSkill private constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment) {
    companion object : SkillCompanionObject {
        private var instance: WizardSkill? = null
        override fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): WizardSkill {
            if (instance == null) {
                instance = WizardSkill(context, enchantment)
            }
            return instance!!
        }
    }

    override val name = "Wizard's Skill"
    override val description = "A stupid skill that all wizards should learn"
    override val usage = "You do nothing and nothing will happen"
    override val key = NamespacedKey(context, "wizard-skill")

    init {
        skills["WizardSkill"] = this
    }

    override fun onDisable() {
        skills.remove("WizardSkill")
    }
}