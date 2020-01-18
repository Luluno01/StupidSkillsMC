package vip.untitled.stupidskills.skills

import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment

abstract class SkillCompanionObject<T : Skill>(val clazz: Class<T>) {
    protected var instance: T? = null
    open fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): T {
        if (instance == null) {
            instance = clazz.getConstructor(JavaPlugin::class.java, SkillEnchantment::class.java)
                .newInstance(context, enchantment)
        }
        return instance!!
    }
}