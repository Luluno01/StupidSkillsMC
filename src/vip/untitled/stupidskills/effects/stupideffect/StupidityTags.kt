package vip.untitled.stupidskills.effects.stupideffect

import com.nametagedit.plugin.NametagEdit
import org.bukkit.ChatColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.stupideffect.events.EntityDiedStupidlyEvent
import vip.untitled.stupidskills.effects.stupideffect.events.StupidMetadataSetEvent
import vip.untitled.stupidskills.helpers.Pluggable
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToInt

open class StupidityTags(protected val context: JavaPlugin, protected val collection: StupidFeatureCollection) :
    Pluggable, Listener {
    companion object {
        private var firstWarn = true
        val defaultStupidTags = arrayOf("Noob", "Naive", "Stupid", "Pro", "Master", "King")
        /**
         * From https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
         */
        private val units = TreeMap<BigInteger, String>()

        init {
            units[1_000L.toBigInteger()] = "k"
            units[1_000_000L.toBigInteger()] = "M"
            units[1_000_000_000L.toBigInteger()] = "G"
            units[1_000_000_000_000L.toBigInteger()] = "T"
            units[1_000_000_000_000_000L.toBigInteger()] = "P"
            units[1_000_000_000_000_000_000L.toBigInteger()] = "E"
            units[BigInteger("1000000000000000000000")] = "?"
        }

        fun formatLevel(level: BigInteger): String {
            if (level < BigInteger.ZERO) {
                return "-${formatLevel(
                    -level
                )}"
            }
            if (level < 1000.toBigInteger()) return level.toString()
            val entry = units.floorEntry(level)
            if (entry.value == "?") return "???"
            val number = (level * BigInteger.TEN / entry.key).toFloat().roundToInt()  // Discard fraction
            return if (number % 10 == 0) {
                "${number / 10}${entry.value}"
            } else "${number / 10.0}${entry.value}"
        }
    }

    var stupidTags =
        defaultStupidTags
    var maxStupidityPerLevel = 256.toBigInteger()

    /**
     * Get stupidity tag
     * @param stupidity Accumulated stupidity
     * @return Stupidity tag
     */
    open fun getStupidityTag(stupidity: BigInteger): String {
        val res = stupidity.divideAndRemainder(maxStupidityPerLevel)
        val quotient = res[0]
        val intQuotient = quotient.toInt()
        val remainder = res[1]
        val size = stupidTags.size
        val bigSize = size.toBigInteger()
        val mainTag = when {
            quotient >= bigSize -> stupidTags[size - 1]
            else -> stupidTags[intQuotient]
        }
        return when {
            quotient >= bigSize -> "$mainTag ${formatLevel(stupidity - (maxStupidityPerLevel * (bigSize - BigInteger.ONE)))}"
            remainder == BigInteger.ZERO -> mainTag
            else -> "${stupidTags[intQuotient]} ${formatLevel(remainder)}"
        }
    }

    open fun getTags(player: LivingEntity): Array<String> {
        val accumulatedStupidity = collection.store.getAccumulatedStupidity(player)
        val prefix = "${ChatColor.RED}[${getStupidityTag(accumulatedStupidity)}]${ChatColor.RESET} "
        val stupidity = collection.stupidMetadataHandler.getStupidity(player)
        val suffix = " ${ChatColor.GOLD}[${if (stupidity > 0) "lv.$stupidity" else "smart"}]${ChatColor.RESET}"
        return arrayOf(prefix, suffix)
    }

    open fun setPlayerTag(player: Player, diedForBeingStupid: Boolean = false) {
        try {
            val tags = getTags(player)
            NametagEdit.getApi().setNametag(
                player,
                tags[0],
                if (diedForBeingStupid) " ${ChatColor.GOLD}[Died for Being Stupid]${ChatColor.RESET} " else tags[1]
            )
        } catch (err: NoClassDefFoundError) {
            if (firstWarn) {
                context.logger.warning("[StupidEffect] NametagEdit is required to enable stupid tags")
                firstWarn = false
            }
        }
    }

    @EventHandler
    open fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val tags = getTags(event.player)
        event.format = event.format.replaceFirst("%1\$s", "${tags[0]}%1\$s${tags[1]}")
    }

    @EventHandler
    open fun onPlayerJoin(event: PlayerJoinEvent) {
        setPlayerTag(event.player)
    }

    @EventHandler
    open fun onStupidMetadataSet(event: StupidMetadataSetEvent) {
        val player = event.entity
        if (player is Player) {
            setPlayerTag(player)
        }
    }

    @EventHandler
    open fun onEntityDiedStupidly(event: EntityDiedStupidlyEvent) {
        val player = event.entity
        if (player is Player) {
            setPlayerTag(player, true)
        }
    }

    override fun onEnable() {
        context.server.pluginManager.registerEvents(this, context)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }
}