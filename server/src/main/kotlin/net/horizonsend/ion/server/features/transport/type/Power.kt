package net.horizonsend.ion.server.features.transport.type

import net.horizonsend.ion.server.features.transport.Wires
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.matchesAxis
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData

class Power : TransportType(
	NamespacedKeys.POWER,
	setOf(Material.SPONGE, Material.END_ROD, Material.IRON_BLOCK, Material.REDSTONE_BLOCK),
	Material.NOTE_BLOCK,
	Material.CRAFTING_TABLE
) {
	override fun canTransfer(isDirectional: Boolean, face: BlockFace, data: BlockData): Boolean {
		return when (data.material) {
			// anything can go into end rod wires, but only if the rotation axis matches
			Material.END_ROD -> Wires.getDirectionalRotation(data).matchesAxis(face)
			// anything can go into directional connectors
			Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
			// anything besides directional connectors can go into sponge wires
			Material.SPONGE -> !isDirectional
			// other stuff is a no
			else -> return false
		}
	}
}
