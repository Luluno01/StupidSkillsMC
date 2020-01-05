package vip.untitled.stupidskills.effects.stupideffect

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import vip.untitled.stupidskills.effects.EffectAdapter
import vip.untitled.stupidskills.effects.stupideffect.events.EntityDiedStupidlyEvent
import vip.untitled.stupidskills.effects.stupideffect.meta.RemainingStupidDurationMetadataValue
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidMetadataValue
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidPigMetadataValue
import vip.untitled.stupidskills.helpers.Pluggable

open class StupidEffect(protected val context: JavaPlugin, protected val key: NamespacedKey) : EffectAdapter(),
    Listener, StupidFeatureCollection, Pluggable {
    companion object {
        const val maxLevel = 10
        protected var instance: StupidEffect? = null
        fun getInstance(context: JavaPlugin? = null, key: NamespacedKey? = null): StupidEffect {
            if (instance == null) {
                instance =
                    StupidEffect(context!!, key!!)
            }
            return instance!!
        }
    }

    override val maxLevel = Companion.maxLevel

    override var store = AccumulatedStupidityStore(context)
    override lateinit var stupidityTags: StupidityTags
    override var stupidMetadataHandler = StupidMetadataValue.Companion.StupidMetadataHandler(context, key)
    override lateinit var stupidState: StupidState
    override lateinit var remainingStupidDurationMetadataHandler: RemainingStupidDurationMetadataValue.Companion.RemainingStupidDurationMetadataHandler
    override var stupidPigHandler = StupidPigMetadataValue.Companion.StupidPigHandler(context, key)

    /* Direct sub-effects */
    protected lateinit var rideAndRiddenByPigEffect: RideAndRiddenByPigEffect
    protected lateinit var stupidCompositePotionEffect: StupidCompositePotionEffect

    /**
     * This should be called by main plugin
     */
    override fun onEnable() {
        store.init()

        stupidityTags = StupidityTags(context, this)
        stupidityTags.onEnable()  // Auto set tag

        stupidState = StupidState(context, this)
        stupidState.onEnable()

        remainingStupidDurationMetadataHandler =
            RemainingStupidDurationMetadataValue.Companion.RemainingStupidDurationMetadataHandler(context, key, this)

        rideAndRiddenByPigEffect = RideAndRiddenByPigEffect(context, this)
        rideAndRiddenByPigEffect.onEnable()  // Auto ride and ridden by pig

        stupidCompositePotionEffect = StupidCompositePotionEffect(context, this)
        stupidCompositePotionEffect.onEnable()  // Auto apply potion effects

        context.server.pluginManager.registerEvents(this, context)
    }

    /**
     * This should be called by main plugin
     */
    override fun onDisable() {
        store.close()
        stupidityTags.onDisable()
        stupidState.onDisable()

        rideAndRiddenByPigEffect.onDisable()
        stupidCompositePotionEffect.onDisable()

        HandlerList.unregisterAll(this)
    }

    @EventHandler
    open fun onEntityDiedStupidly(event: EntityDiedStupidlyEvent) {
        // Broadcast
        val entity = event.entity
        if (entity is Player) {
            val stupidity = stupidMetadataHandler.getStupidity(entity)
            if (stupidity > 0) {
                Bukkit.broadcastMessage("${ChatColor.AQUA}${entity.name}${ChatColor.RESET} died stupidly ${ChatColor.GOLD}(level $stupidity)")
            }
        }
    }

    override fun apply(entity: Entity, context: JavaPlugin, level: Int) {
        apply(entity, level)
    }

    /**
     * Apply stupid effect on an entity
     * @param entity Target entity
     * @param level Stupid level
     * @return Whether the effect is successfully applied
     */
    open fun apply(entity: Entity, level: Int): Boolean {
        if (entity !is LivingEntity) return false
        if (stupidPigHandler.isStupidPig(entity)) return false
        val stupidity = stupidMetadataHandler.getStupidity(entity)
        val lvl = sanitizeLevel(level)
        if (stupidity > lvl) return false  // Don not override higher level
        stupidState.apply(entity, level, stupidMetadataHandler)
        if (entity is Player) {
            when (stupidity) {
                0 -> entity.chat("${ChatColor.GREEN}i am sTuPiD")  // Make sure this looks stupid
                in 1..3 -> entity.chat("${ChatColor.AQUA}i am eVen morE SDOOOOPId")  // Make sure this looks even more stupid
                in 4..6 -> entity.chat("${ChatColor.GOLD}I BEcome more stooobId than ever")  // Make sure this looks more stupider than ever
                else -> entity.chat("${ChatColor.MAGIC}I AM THE KING OF STUPIDITY")  // You get it, right?
            }
        }
        return true
    }
}