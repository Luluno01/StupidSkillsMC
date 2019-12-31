package vip.untitled.stupidskills.helpers

import org.bukkit.entity.LivingEntity

class LookingAt {
    companion object {
        fun what(
            entity: LivingEntity,
            maxDistance: Int,
            ignoreNonLivingEntity: Boolean = true
        ): Any? /* Entity, Block or null */ {
            val targetBlock = entity.getTargetBlock(maxDistance)
            var targetEntity = entity.getTargetEntity(maxDistance)
            if (ignoreNonLivingEntity && targetEntity !is LivingEntity) targetEntity = null
            if (targetBlock == null) {
                return targetEntity
            } else {
                return if (targetEntity == null) {
                    // !null, null
                    targetBlock
                } else {
                    // !null, !null
                    val sourceLocation = entity.location
                    val blockLocation = targetBlock.location
                    val entityLocation = targetEntity.location
                    return if (sourceLocation.distance(blockLocation) < sourceLocation.distance(entityLocation)) {
                        targetBlock
                    } else {
                        targetEntity
                    }
                }
            }
        }
    }
}