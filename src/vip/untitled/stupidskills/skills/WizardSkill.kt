package vip.untitled.stupidskills.skills

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

class WizardSkill constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment) {
    companion object : SkillCompanionObject<WizardSkill>(WizardSkill::class.java)

    override val name = "Wizard's Skill"
    override val internalName = "WizardSkill"
    override val description = "A stupid skill that all wizards should learn"
    override val usage = "You do nothing and nothing will happen"
    override val key = NamespacedKey(context, "wizard-skill")
}