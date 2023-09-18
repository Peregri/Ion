package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Star
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.block.data.BlockData

class CachedStar(
	val databaseId: Oid<Star>,
	override val name: String,
	spaceWorldName: String,
	location: Vec3i,
	size: Double,
	override val seed: Long,
	val layers: List<CachedCrustLayer>,
) : CelestialBody(spaceWorldName, location),
	NamedCelestialBody,
	NoiseBasedSurfaceProvider {
	companion object {
		private const val MAX_SIZE = 190
	}

	init {
		require(size > 0 && size <= 1)
	}

	val sphereRadius = (MAX_SIZE * size).toInt()

	override fun createStructure(): Map<Vec3i, BlockState> {
		val structure = mutableMapOf<Vec3i, BlockState>()

		var offset = 0

		for ((separation, noise, materials) in layers) {
			offset += separation

			structure += createCrust(sphereRadius + offset, noise, materials)
		}

		val dirt = Material.DIRT.createBlockData().nms
		if (layers.isEmpty()) structure += getSphereBlocks(sphereRadius).associateWith { dirt }

		return structure
	}

	data class CachedCrustLayer(
		val seperation: Int,
		val noise: Double,
		val materials: List<BlockData>
	)
}
