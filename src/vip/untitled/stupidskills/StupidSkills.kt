package vip.untitled.stupidskills

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.StupidEffect
import vip.untitled.stupidskills.helpers.TickCounter
import vip.untitled.stupidskills.skills.*
import java.lang.Integer.parseInt

class StupidSkills : JavaPlugin(), TickCounter.Companion.TickCounterOwner {
    private val skillClasses = arrayOf(WizardSkill, SteveCannon, HomingMissile, JavelinMissile, Stupefy)
    private val skillEnchantmentKey = NamespacedKey(this, "skill")
    private val skillEnchantment = SkillEnchantment(skillEnchantmentKey)

    override lateinit var tickCounter: TickCounter

    override fun onEnable() {
        super.onEnable()
        skillEnchantment.registerSelf()
        for (skillClass in skillClasses) {
            val skill = skillClass.getInstance(this, skillEnchantment).onEnable()
            logger.info("${ChatColor.GREEN}Registering ${ChatColor.GOLD}${skill.name}: ${ChatColor.GREEN}${skill.description}")
        }
        enableEffects()
        logger.info("StupidSkills enabled")
        logger.info(ChatColor.GOLD.toString() + "${skillClasses.size} skill(s) enabled")
        tickCounter = TickCounter(this)
    }

    private fun enableEffects() {
        server.pluginManager.registerEvents(
            StupidEffect.getInstance(
                this,
                Stupefy.getInstance(this, skillEnchantment).key
            ), this
        )
    }

    override fun onDisable() {
        super.onDisable()
        for (skillClass in skillClasses) {
            skillClass.getInstance(this, skillEnchantment).onDisable()
        }
        HandlerList.unregisterAll(this)  // Remove all listeners including those added by effects
        logger.info("StupidSkills disabled")
        tickCounter.cancel()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (command.name) {
            "skill" -> onSkillCommand(sender, args)
            "list-skill" -> onListSkillCommand(sender)
            /* Skill specific commands */
            "is-stupid" -> onIsStupidCommand(sender, args)
            else -> false
        }
    }

    private fun onListSkillCommand(sender: CommandSender): Boolean {
        // /list-skill
        for ((skillName, skillDescriptor) in Skill.skills) {
            sender.sendMessage("${ChatColor.GOLD}$skillName: ${ChatColor.AQUA}${skillDescriptor.name}${ChatColor.RESET} ${skillDescriptor.description}")
        }
        return true
    }

    private fun onSkillCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /skill <skill-name> <level> [player]
        if (!sender.isOp) {
            sender.sendMessage(ChatColor.RED.toString() + "You must be an OP to do this")
            return true
        }
        val argc = args.size
        if (argc != 2 && argc != 3) {
            return false
        }
        // Skill name
        val skillName = args[0]  // This is ensured
        // Skill level
        val level: Int
        try {
            level = parseInt(args[1])
        } catch (err: NumberFormatException) {
            return false
        }
        // Target player
        var player: Player? = null
        if (sender is Player) player = sender  // Default target player: self
        if (args.size == 3) {
            player = Bukkit.getPlayerExact(args[2])  // Override target player, specified by name
        }
        if (player == null) {
            sender.sendMessage("Cannot make the console learn the skill, or no such player")
            return true
        }
        // Give `player` a custom skill item
        val skill = Skill.forName(skillName)
        if (skill == null) {
            sender.sendMessage(ChatColor.RED.toString() + "No such skill")
            return true
        } else {
            player.inventory.addItem(skill.getItem(player, level))
            player.sendActionBar("Congratulations! You have learnt " + ChatColor.GOLD + skill.name)
        }
        return true
    }

    private fun onIsStupidCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /is-stupid [player]
        val player = if (args.isNotEmpty()) {
            Bukkit.getPlayer(args[0])
        } else {
            if (sender is Player) {
                sender
            } else {
                sender.sendMessage("${ChatColor.RED}You must be a player to use this command")
                return true
            }
        }
        if (player == null) {
            sender.sendMessage("${ChatColor.RED}No such player")
        } else {
            val stupidity = StupidEffect.getInstance().getStupidity(player)
            if (stupidity > 0) {
                if (player == sender) sender.sendMessage("Yes, you are (level ${stupidity})")
                else sender.sendMessage("Yes, player ${ChatColor.AQUA}${player.displayName}${ChatColor.RESET} is stupid (level ${stupidity})")
            } else {
                if (player == sender) sender.sendMessage("Your are not stupid enough! Try harder!")
                else sender.sendMessage("Player ${ChatColor.AQUA}${player.displayName}${ChatColor.RESET} is being smart")
            }
        }
        return true
    }
}