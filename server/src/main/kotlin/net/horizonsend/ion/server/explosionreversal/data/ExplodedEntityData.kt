package net.horizonsend.ion.server.explosionreversal.data

import net.horizonsend.ion.server.explosionreversal.nms.NMSUtils
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

class ExplodedEntityData(
    val entityType: EntityType, val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float,
    val explodedTime: Long, val nmsData: ByteArray?
) {

    constructor(entity: Entity, explosionTime: Long) : this(
        entity.type, entity.location.x, entity.location.y, entity.location.z,
        entity.location.pitch, entity.location.yaw,
        explosionTime, NMSUtils.getEntityData(entity)
    )
}
