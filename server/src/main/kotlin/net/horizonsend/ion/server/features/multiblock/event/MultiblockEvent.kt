package net.horizonsend.ion.server.features.multiblock.event

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.event.Event

abstract class MultiblockEvent<T: Multiblock>(
	open val multiblock: T
) : Event()
