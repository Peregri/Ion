package net.horizonsend.ion.server.features.multiblock.event

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.entity.Player

abstract class PlayerMultiblockEvent<T: Multiblock>(
	override val multiblock: T,
	open val player: Player
) : MultiblockEvent<T>(multiblock) {
}
