package net.horizonsend.ion.server.features.multiblock

import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.transport.type.TransportType
import org.bukkit.block.Sign

/** General interface for a multiblock that can store a value **/
interface StoringMultiblock {
	val maxStoredValue: Int

	// Checks if the multiblock can take an input of power
	fun canTake(transportType: TransportType<*>, cache: LoadingCache<Sign, Int>, destinationSign: Sign): Int? {
		val destinationValue = cache[destinationSign]
		val destinationValueMax = maxStoredValue

		return destinationValueMax - destinationValue
	}
}

