package net.horizonsend.ion.server.features.transport.type

import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.features.transport.Transports
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.matchesAxis
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

object Power : TransportType<PowerStoringMultiblock>() {
	override val namespacedKey = NamespacedKeys.POWER
	override val transportBlocks = setOf(Material.SPONGE, Material.END_ROD, Material.IRON_BLOCK, Material.REDSTONE_BLOCK)
	override val inputBlock = Material.NOTE_BLOCK
	override val extractionBlock = Material.CRAFTING_TABLE

	override val prefixComponent: Component = Component.text("E: ", NamedTextColor.YELLOW)
	override val textColor: TextColor = NamedTextColor.GREEN

	override val storedLine: Int = 1

	override val setIfEmpty: Boolean = true

	override val offsets: Set<Vec3i> = setOf(
		// most multiblocks have the sign a block up and out of the computer
		Vec3i(1, 1, 0), Vec3i(-1, 1, 0), Vec3i(0, 1, -1), Vec3i(0, 1, 1),
		// power cells have it on the block
		Vec3i(1, 0, 0), Vec3i(-1, 0, 0), Vec3i(0, 0, -1), Vec3i(0, 0, 1),
		// drills have it on a corner
		Vec3i(-1, 0, -1), Vec3i(1, 0, -1), Vec3i(1, 0, 1), Vec3i(-1, 0, 1),
		// upside down mining lasers have signs below
		Vec3i(1, -1, 0), Vec3i(-1, -1, 0), Vec3i(0, -1, -1), Vec3i(0, -1, 1),
	)

	override fun checkStep(direction: BlockFace, nextType: Material): Set<BlockFace> = when (nextType) {
			Material.END_ROD -> setOf(direction)
			Material.SPONGE, Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> ADJACENT_BLOCK_FACES
			else -> setOf() // if it's not one of the above blocks it's not a wire block, so end the wire chain
		}


	/**
	 * @param isDirectional If the origin wire is a directional wire
	 * @param face The direction the origin wire was heading
	 * @param data The data of the next wire
	 */
	override fun canTransfer(isDirectional: Boolean, face: BlockFace, data: BlockData): Boolean {
		return when (data.material) {
			// anything can go into end rod wires, but only if the rotation axis matches
			Material.END_ROD -> Transports.getDirectionalRotation(data).matchesAxis(face)
			// anything can go into directional connectors
			Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
			// anything besides directional connectors can go into sponge wires
			Material.SPONGE -> !isDirectional
			// other stuff is a no
			else -> return false
		}
	}
}
