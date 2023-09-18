package net.horizonsend.ion.server.features.multiblock

import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.transport.type.TransportType
import org.bukkit.block.Sign

/** General interface for a multiblock that can store a value **/
interface StoringMultiblock {
	val maxStored: Int

	// Checks if the multiblock can take an input of power
	fun canTake(transportType: TransportType<*>, cache: LoadingCache<Sign, Int>, destinationSign: Sign): Int? {
		val destinationPower = cache[destinationSign]
		val destinationPowerMax = maxStored

		return destinationPowerMax - destinationPower
	}
}

