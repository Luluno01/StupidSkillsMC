package vip.untitled.stupidskills.skills

import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import vip.untitled.stupidskills.SkillEnchantment
import vip.untitled.stupidskills.effects.AreaDamageEffect
import vip.untitled.stupidskills.effects.ExplosionEffect
import vip.untitled.stupidskills.effects.FullAreaDamageEffect
import java.util.*

open class HomingMissile constructor(context: JavaPlugin, enchantment: SkillEnchantment) :
    Skill(context, enchantment) {
    companion object : SkillCompanionObject<HomingMissile>(HomingMissile::class)

    override val name = "Homing Missile"
    override val internalName = "HomingMissile"
    override val description = "Launch a homing missile"
    override val usage = "Look at the target and left/right click"
    override val key = NamespacedKey(context, "homing-missile-skill")

    protected val homingTasks = hashMapOf<UUID, HomingMissileTask>()

    protected open fun getHomingMeta(level: Int): MetadataValueAdapter {
        return object : MetadataValueAdapter(context) {
            override fun value(): Int {
                return level
            }

            override fun invalidate() {
                return
            }
        }
    }

    @Suppress("LeakingThis")
    protected abstract class HomingMissileTask(
        protected val plugin: JavaPlugin,
        protected val skill: HomingMissile,
        metadataValue: MetadataValueAdapter,
        val arrow: Arrow,
        val torque: Float,
        val maxVelocity: Double
    ) : BukkitRunnable() {
        private val level = metadataValue.value() as Int
        var targetOverride: Vector? = null

        init {
            arrow.setMetadata(skill.key.toString(), metadataValue)
            skill.homingTasks[arrow.uniqueId] = this
            runTaskTimer(plugin, 0L, 1L)
        }

        abstract fun getToTargetVector(): Vector
        abstract fun isTargetAlive(): Boolean

        override fun run() {
            if (arrow.isDead) {
                // The missile is removed by other commands/plugins
                cancel()
                return
            }
            if (isTargetAlive()) {
                val toTarget = if (targetOverride == null) getToTargetVector()
                else targetOverride!!.clone().subtract(arrow.location.toVector())
                if (toTarget.length() < 0.5) {
                    // Tango hit
                    skill.applyEffect(arrow, null, level)
                    cancel()
                    return
                }
                val newVelocity = arrow.velocity.clone().add(toTarget.normalize().multiply(torque))
                if (newVelocity.length() > maxVelocity) newVelocity.normalize().multiply(maxVelocity)
                arrow.velocity = newVelocity
            }
            // else drop it
            arrow.world.playEffect(arrow.location, Effect.SMOKE, 0)
        }

        override fun cancel() {
            skill.homingTasks.remove(arrow.uniqueId)
            arrow.removeMetadata(skill.key.toString(), plugin)
            arrow.remove()
            super.cancel()
        }
    }

    protected open class EntityHomingMissileTask(
        plugin: JavaPlugin,
        skill: HomingMissile,
        metadataValue: MetadataValueAdapter,
        arrow: Arrow,
        protected val target: LivingEntity,
        torque: Float,
        maxVelocity: Double
    ) : HomingMissileTask(plugin, skill, metadataValue, arrow, torque, maxVelocity) {
        override fun getToTargetVector(): Vector {
            return target.location.toVector().subtract(arrow.location.toVector())
        }

        override fun isTargetAlive(): Boolean {
            return !target.isDead
        }
    }

    protected open class BlockHomingMissileTask(
        plugin: JavaPlugin,
        skill: HomingMissile,
        metadataValue: MetadataValueAdapter,
        arrow: Arrow,
        target: Block,
        torque: Float,
        maxVelocity: Double
    ) : HomingMissileTask(plugin, skill, metadataValue, arrow, torque, maxVelocity) {
        protected var targetLocation = target.location
        protected var targetWorld = target.world
        override fun getToTargetVector(): Vector {
            return targetLocation.toVector().subtract(arrow.location.toVector())
        }

        override fun isTargetAlive(): Boolean {
            return !targetWorld.getBlockAt(targetLocation).isEmpty
        }
    }

    protected open fun onNewMissile(missile: Arrow, homingMissileTask: HomingMissileTask) {
        return
    }

    override fun cast(caster: Entity, level: Int): Boolean {
        if (caster is LivingEntity) {
            val targetBlock = caster.getTargetBlock(64)
            var targetEntity = caster.getTargetEntity(64)
            if (targetEntity !is LivingEntity) targetEntity =
                null  // We don't want to fire a missile at a flying snowball
            if (targetBlock == null) {
                return if (targetEntity == null) {
                    // null, null
                    true
                } else {
                    // null, !null
                    cast(caster, targetEntity as LivingEntity, level)
                    true
                }
            } else {
                return if (targetEntity == null) {
                    // !null, null
                    cast(caster, targetBlock, level)
                    true
                } else {
                    // !null, !null
                    val casterLocation = caster.location
                    val blockLocation = targetBlock.location
                    val entityLocation = targetEntity.location
                    if (casterLocation.distance(blockLocation) < casterLocation.distance(entityLocation)) {
                        cast(caster, targetBlock, level)
                        true
                    } else {
                        cast(caster, targetEntity as LivingEntity, level)
                        true
                    }
                }
            }
        }
        return true
    }

    protected open fun sendMessage(entity: Entity) {
        if (entity is Player) {
            entity.sendActionBar(ChatColor.GOLD.toString() + "Homing Missile launched!")
        }
    }

    protected open fun getTorque(level: Int): Float {
        return 0.8f + 0.4f * level
    }

    protected open fun getMaxVelocity(level: Int): Double {
        return 1 + 0.25 * level
    }

    open fun cast(caster: LivingEntity, target: Block, level: Int) {
        if (target.isEmpty) return
        sendMessage(caster)
        val arrow = caster.launchProjectile(Arrow::class.java, caster.location.direction)
        val task = BlockHomingMissileTask(
            context,
            this,
            getHomingMeta(level),
            arrow,
            target,
            getTorque(level),
            getMaxVelocity(level)
        )
        onNewMissile(arrow, task)
    }

    open fun cast(caster: LivingEntity, target: LivingEntity, level: Int) {
        sendMessage(caster)
        val arrow = caster.launchProjectile(Arrow::class.java, caster.location.direction)
        val task = EntityHomingMissileTask(
            context,
            this,
            getHomingMeta(level),
            arrow,
            target,
            getTorque(level),
            getMaxVelocity(level)
        )
        onNewMissile(arrow, task)
    }

    @EventHandler
    open fun onArrowHit(event: ProjectileHitEvent) {
        val arrow = event.entity
        if (arrow is Arrow) {
            for (meta in arrow.getMetadata(key.toString())) {
                val level = meta.value()
                if (level is Int && level > 0) {
                    // This is a homing missile
                    arrow.remove()
                    // Get damage source
                    val shooter: LivingEntity? = if (arrow.shooter is LivingEntity) {
                        arrow.shooter as LivingEntity
                    } else {
                        null
                    }
                    // Get target
                    val hitBlock = event.hitBlock
                    val hitEntity = event.hitEntity
                    // Apply effect
                    if (hitBlock != null) {
                        applyEffect(arrow, shooter, level)
                    } else if (hitEntity != null) {
                        applyEffect(hitEntity, shooter, level)
                    }
                }
            }
            homingTasks[arrow.uniqueId]?.cancel()
        }
    }

    protected fun applyEffect(entity: Entity, source: Entity?, level: Int) {
        ExplosionEffect().apply(entity, context, level)
        FullAreaDamageEffect().apply(entity, source, level)
    }

    protected fun applyEffect(arrow: Arrow, source: Entity?, level: Int) {
        ExplosionEffect().apply(arrow, context, level)
        AreaDamageEffect().apply(arrow, source, level)
    }
}