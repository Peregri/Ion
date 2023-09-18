package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.gas.type.Gas

interface GasStoringMultiblock : StoringMultiblock {
	override val maxStored: Int

	val storableGasses: List<Gas>
}
