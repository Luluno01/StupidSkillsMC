package vip.untitled.stupidskills

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.stupideffect.StupidEffect
import vip.untitled.stupidskills.helpers.TickCounter
import vip.untitled.stupidskills.skills.*
import java.lang.Integer.parseInt
import java.math.BigInteger

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
        StupidEffect.getInstance(
            this,
            Stupefy.getInstance(this, skillEnchantment).key
        ).onEnable()
    }

    private fun disableEffects() {
        StupidEffect.getInstance(
            this,
            Stupefy.getInstance(this, skillEnchantment).key
        ).onDisable()
    }

    override fun onDisable() {
        super.onDisable()
        for (skillClass in skillClasses) {
            skillClass.getInstance(this, skillEnchantment).onDisable()
        }
        disableEffects()
        logger.info("StupidSkills disabled")
        tickCounter.cancel()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (command.name) {
            "skill" -> onSkillCommand(sender, args)
            "list-skill" -> onListSkillCommand(sender)
            /* Skill specific commands */
            "is-stupid" -> onIsStupidCommand(sender, args)
            "stupidity" -> onStupidityCommand(sender, args)
            "clear-stupidity" -> onClearStupidityCommand(sender, args)
            "set-stupidity" -> onSetStupidityCommand(sender, args)
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

    private fun getPlayerInCommand(sender: CommandSender, playerArg: String?): Player? {
        val player = if (playerArg == null) {
            if (sender is Player) {
                sender
            } else {
                sender.sendMessage("${ChatColor.RED}You must be a player to use this command")
                return null
            }
        } else {
            Bukkit.getPlayer(playerArg)
        }
        if (player == null) {
            sender.sendMessage("${ChatColor.RED}No such player or player offline")
        }
        return player
    }

    private fun onIsStupidCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /is-stupid [player]
        val player = getPlayerInCommand(sender, if (args.isEmpty()) null else args[0])
        if (player != null) {
            val stupidity = StupidEffect.getInstance().stupidMetadataHandler.getStupidity(player)
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

    private fun onStupidityCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /stupidity [player]
        val player = getPlayerInCommand(sender, if (args.isEmpty()) null else args[0])
        if (player != null) {
            sender.sendMessage(
                "Player ${ChatColor.AQUA}${player.displayName}${ChatColor.RESET} has a stupidity of ${StupidEffect.getInstance().store.getAccumulatedStupidity(
                    player
                )}"
            )
        }
        return true
    }

    private fun onClearStupidityCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /clear-stupidity [player]
        val player = getPlayerInCommand(sender, if (args.isEmpty()) null else args[0])
        if (player != null) {
            val collection = StupidEffect.getInstance()
            collection.store.clearAccumulatedStupidity(player)
            collection.stupidityTags.setPlayerTag(player)
            sender.sendMessage("Stupidity of player ${ChatColor.AQUA}${player.displayName}${ChatColor.RESET} cleared")
        }
        return true
    }

    private fun onSetStupidityCommand(sender: CommandSender, args: Array<out String>): Boolean {
        // /set-stupidity <stupidity> [player]
        if (args.isEmpty()) return false
        val stupidity: BigInteger
        try {
            stupidity = BigInteger(args[0])
        } catch (err: NumberFormatException) {
            return false
        }
        if (stupidity < BigInteger.ZERO) {
            sender.sendMessage("${ChatColor.RED}Stupidity must be a non-negative number")
            return true
        }
        val player = getPlayerInCommand(sender, if (args.size < 2) null else args[1])
        if (player != null) {
            val collection = StupidEffect.getInstance()
            collection.store.setAccumulatedStupidity(player, stupidity)
            collection.stupidityTags.setPlayerTag(player)
            sender.sendMessage("Stupidity of player ${ChatColor.AQUA}${player.displayName}${ChatColor.RESET} set to ${args[0]}")
        }
        return true
    }
}