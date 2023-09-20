package net.horizonsend.ion.server.features.transport.type

import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.GasStoringMultiblock
import net.horizonsend.ion.server.features.transport.Transports
import net.horizonsend.ion.server.features.transport.colorMap
import net.horizonsend.ion.server.features.transport.isColoredPipe
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.randomEntry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class GasTransport(val gas: Gas, displayLine: Int) : TransportType<GasStoringMultiblock>() {
	override val extractionBlock: Material = Material.CRAFTING_TABLE
	override val inputBlock: Material = Material.SMITHING_TABLE

	override val namespacedKey: NamespacedKey = gas.namespacedKey
	override val offsets: Set<Vec3i> = Power.offsets //TODO

	override val prefixComponent: Component = gas.displayName.append(text(": ", NamedTextColor.RED))
	override val textColor: TextColor = NamedTextColor.GOLD

	override val transportBlocks: Set<Material> = GLASS_TYPES.apply { addAll(GLASS_PANE_TYPES) }

	override val storedLine: Int = displayLine

	override val setIfEmpty: Boolean = false

	override fun checkStep(direction: BlockFace, nextType: Material): Set<BlockFace> = when {
		nextType.isGlass -> ADJACENT_BLOCK_FACES
		nextType.isGlassPane -> setOf(direction)
		else -> setOf()
	}

	override fun isDirectional(isDirectional: Material): Boolean = isDirectional.isGlassPane

	override fun pickDirection(isDirectional: Boolean, adjacentWires: Set<BlockFace>, direction: BlockFace): BlockFace = when {
		// go straight if possible when directional
		isDirectional && adjacentWires.contains(direction) -> direction

		// normally, pick a random direction to go in
		else -> adjacentWires.randomEntry()
	}

	override fun canTransfer(originType: Material, isDirectional: Boolean, face: BlockFace, data: BlockData): Boolean {
		val otherType = data.material

		if (originType == inputBlock || otherType == inputBlock) return true
		if (originType == extractionBlock || otherType == extractionBlock) return true

		return Pipes.isAnyPipe(otherType) && // it has to be any of the valid pipe types
			(isColoredPipe(originType) == isColoredPipe(otherType)) && // both are either colored pipes or not
			colorMap[originType] == colorMap[otherType]
	}

	companion object {
		operator fun get(gas: Gas) = Transports.transportTypes.firstOrNull { it is GasTransport && it.gas == gas } as GasTransport?
	}
}
