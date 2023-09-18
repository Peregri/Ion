package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.d
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator

interface NoiseBasedSurfaceProvider {
	val seed: Long

	fun getNoise(): SimplexNoiseGenerator  = SimplexNoiseGenerator(seed)
	fun getCrustPalette(crustMaterials: List<BlockData>): List<BlockState> = crustMaterials.map(BlockData::nms)

	fun createCrust(crustRadius: Int, crustNoise: Double, materials: List<BlockData>): Map<Vec3i, BlockState> {
		val random = getNoise()
		val crustPalette = getCrustPalette(materials)

		return getSphereBlocks(crustRadius).associateWith { (x, y, z) ->
			// number from -1 to 1
			val simplexNoise = random.noise(x.d() * crustNoise, y.d() * crustNoise, z.d() * crustNoise)

			val noise = (simplexNoise / 2.0 + 0.5)

			return@associateWith when {
				crustPalette.isEmpty() -> Blocks.DIRT.defaultBlockState()
				else -> crustPalette[(noise * crustPalette.size).toInt()]
			}
		}
	}
}
