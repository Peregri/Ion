package net.horizonsend.ion.server.explosionreversal

import net.horizonsend.ion.server.explosionreversal.data.ExplodedBlockData
import net.horizonsend.ion.server.explosionreversal.data.ExplodedEntityData
import net.horizonsend.ion.server.explosionreversal.nms.NMSUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Painting
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

object Regeneration {
    private val logger = Logger.getLogger(Regeneration::class.java.name)
    @JvmStatic
	@Throws(IOException::class)
    fun pulse(plugin: ExplosionReversalPlugin) {
        regenerateBlocks(plugin, false)
        regenerateEntities(plugin, false)
    }

    @JvmStatic
	@Throws(IOException::class)
    fun regenerateBlocks(plugin: ExplosionReversalPlugin, instant: Boolean): Int {
        val millisecondDelay = plugin.settings?.regenDelay?.times( 60L * 1000L)?.toLong() ?: return 0
        val maxNanos = plugin.settings?.placementIntensity?.times( 1000000L)?.toLong() ?: return 0
        val start = System.nanoTime()
        var regenerated = 0
        for (world in Bukkit.getWorlds()) {
            val blocks = plugin.worldData?.getBlocks(world) ?: return 0
            val iterator = blocks.iterator()
            while (iterator.hasNext()) {
                val data = iterator.next()
                if (!instant) {
                    if (System.nanoTime() - start > maxNanos) { // i.e. taking too long
                        return regenerated
                    }
                    if (System.currentTimeMillis() - data.explodedTime < millisecondDelay) {
                        continue
                    }
                }
                iterator.remove()
                regenerateBlock(world, data)
                regenerated++
            }
        }
        return regenerated
    }

    @Throws(IOException::class)
    private fun regenerateBlock(world: World, data: ExplodedBlockData) {
        val block = world.getBlockAt(data.x, data.y, data.z)
        val blockData = data.blockData
        block.setBlockData(blockData, false)
        val tileData = data.tileData
        if (tileData != null) {
            NMSUtils.setTileEntity(block, tileData)
        }
    }

    @JvmStatic
	@Throws(IOException::class)
    fun regenerateEntities(plugin: ExplosionReversalPlugin, instant: Boolean): Int {
        val millisecondDelay = (plugin.settings?.regenDelay?.times(60L)?.times(1000L))?.toLong() ?: return 0
        var regenerated = 0
        for (world in Bukkit.getWorlds()) {
            val entities = plugin.worldData?.getEntities(world)
            val iterator = entities?.iterator() ?: return 0
            while (iterator.hasNext()) {
                val data = iterator.next()
                if (!instant && System.currentTimeMillis() - data.explodedTime < millisecondDelay) {
                    continue
                }
                regenerateEntity(world, data)
                iterator.remove()
                regenerated++
            }
        }
        return regenerated
    }

    @Throws(IOException::class)
    private fun regenerateEntity(world: World, data: ExplodedEntityData) {
        val location = Location(world, data.x, data.y, data.z, data.pitch, data.yaw)
        val entityType = data.entityType
		val entity: Entity = try {
			world.spawnEntity(location, entityType)
		} catch (exception: IllegalArgumentException) {
			logger.log(
				Level.SEVERE, "Failed to regenerate " + entityType
						+ " at " + data.x + ", " + data.y + ", " + data.z, exception
			)
			return
		}
        val nmsData = data.nmsData
        if (nmsData != null) {
            NMSUtils.restoreEntityData(entity, nmsData)
        }
        if (entity is LivingEntity) {
			entity.health = entity.health
		}
        if (entity is Painting) {
			entity.setArt(entity.art, true)
		}
    }
}
