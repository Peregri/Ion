package net.horizonsend.ion.server.explosionreversal.listener

import net.horizonsend.ion.server.explosionreversal.ExplosionReversalPlugin
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.hanging.HangingBreakEvent
import java.io.IOException
import java.util.UUID
import kotlin.collections.HashMap
import kotlin.math.roundToLong
import net.horizonsend.ion.server.explosionreversal.data.ExplodedEntityData as ExplodedEntityData1

class EntityListener(private val plugin: ExplosionReversalPlugin) : Listener {
    private val pendingDeathEntities = HashMap<UUID, ExplodedEntityData1>()

    // put the data in beforehand because some entities such as armor stands lose items before the death event but
    // after this event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @Throws(
        IOException::class
    )
    fun onEntityDemise(event: EntityDamageEvent) {
        val entity = event.entity
        if (!isRegeneratedEntity(entity)) {
            return
        }
        if (!isCausedByExplosion(event)) {
            return
        }
        val explodedEntityData = getExplodedEntityData(entity)
        pendingDeathEntities[entity.uniqueId] = explodedEntityData

        // items don't call entity death event
        if (entity.type == EntityType.DROPPED_ITEM) {
            onEntityExplode(entity)
            entity.remove()
        }
    }

    @Throws(IOException::class)
    private fun getExplodedEntityData(entity: Entity): ExplodedEntityData1 {
        val cap = plugin.settings!!.getDistanceDelayCap()
        val delay = plugin.settings!!.distanceDelay
        val time = System.currentTimeMillis() + (cap * delay * 1000L).roundToLong()
        return ExplodedEntityData1(entity, time)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @Throws(IOException::class)
    fun onItemDeath(event: ItemDespawnEvent) {
        val entity = event.entity
        if (!isRegeneratedEntity(entity) || !isCausedByExplosion(entity.lastDamageCause)) {
            return
        }
        onEntityExplode(entity)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @Throws(IOException::class)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (!isRegeneratedEntity(entity) || !isCausedByExplosion(entity.lastDamageCause)) {
            return
        }
        onEntityExplode(entity)
        event.drops.clear()
    }

    @Throws(IOException::class)
    private fun onEntityExplode(entity: Entity) {
        val id = entity.uniqueId
		val explodedEntityData: ExplodedEntityData1? = if (pendingDeathEntities.containsKey(id)) {
			pendingDeathEntities.remove(id)
		} else {
			getExplodedEntityData(entity)
		}
        val world = entity.world
        plugin.worldData!!.addEntity(world, explodedEntityData!!)
    }

    private fun isRegeneratedEntity(entity: Entity): Boolean {
        val type = entity.type
        if (plugin.settings!!.ignoredEntities.contains(type)) {
            return false
        }
        return if (plugin.settings!!.includedEntities.contains(type)) {
            true
        } else when (type) {
            EntityType.ARMOR_STAND, EntityType.PAINTING -> true
            else -> false
        }
    }

    private fun isCausedByExplosion(event: EntityDamageEvent?): Boolean {
        return if (event == null) {
            false
        } else when (event.cause) {
            DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION -> true
            else -> false
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Throws(
        IOException::class
    )
    fun onPaintingBreak(event: HangingBreakEvent) {
        if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return
        }
        val entity = event.entity
        if (!isRegeneratedEntity(entity)) {
            return
        }
        event.isCancelled = true
        val explodedEntityData = getExplodedEntityData(entity)
        plugin.worldData!!.addEntity(entity.world, explodedEntityData)
        entity.remove()
    }
}
