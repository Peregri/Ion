package net.horizonsend.ion.server.features.transport.type

import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.GasStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class GasTransport(val gas: Gas, displayLine: Int) : TransportType<GasStoringMultiblock>() {
	override val extractionBlock: Material = Material.SMITHING_TABLE
	override val inputBlock: Material = Material.CARTOGRAPHY_TABLE

	override val namespacedKey: NamespacedKey = gas.namespacedKey
	override val offsets: Set<Vec3i> = setOf() //TODO

	override val prefixComponent: Component = text("G: ", NamedTextColor.RED)
	override val textColor: TextColor = NamedTextColor.GOLD

	override val transportBlocks: Set<Material> = STAINED_GLASS_TYPES.apply { addAll(STAINED_GLASS_PANE_TYPES) }

	override val storedLine: Int = displayLine

	override fun canTransfer(isDirectional: Boolean, face: BlockFace, data: BlockData): Boolean {
		return isDirectional
	}
}
