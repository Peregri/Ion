package net.horizonsend.ion.server.features.multiblock

interface PowerStoringMultiblock : StoringMultiblock {
	override val maxStored: Int
}
