package net.horizonsend.ion.server.features.multiblock

import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.type.GasTransport
import net.horizonsend.ion.server.features.transport.type.TransportType
import org.bukkit.block.Sign

interface GasStoringMultiblock : StoringMultiblock {
	override val maxStored: Int

	val storableGasses: List<Gas>

	// Theres probably a better way of doing this
	override fun canTake(transportType: TransportType<*>, cache: LoadingCache<Sign, Int>, destinationSign: Sign): Int? {
		if (transportType !is GasTransport) return null

		if (!storableGasses.contains(transportType.gas)) return null

		return super.canTake(transportType, cache, destinationSign)
	}
}
