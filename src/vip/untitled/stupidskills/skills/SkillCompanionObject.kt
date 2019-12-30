package vip.untitled.stupidskills.skills

import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.SkillEnchantment
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class SkillCompanionObject<T : Skill>(val clazz: KClass<T>) {
    protected var instance: T? = null
    open fun getInstance(context: JavaPlugin, enchantment: SkillEnchantment): T {
        if (instance == null) {
            instance = clazz.primaryConstructor!!.call(context, enchantment)
        }
        return instance!!
    }
}