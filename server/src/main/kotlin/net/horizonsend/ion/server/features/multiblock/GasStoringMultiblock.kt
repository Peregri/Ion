package net.horizonsend.ion.server.features.multiblock

import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.transport.type.GasTransport
import net.horizonsend.ion.server.features.transport.type.TransportType
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType

interface GasStoringMultiblock : StoringMultiblock {
	override val maxStoredValue: Int
	val maxGasTypes: Int

	val storableGasses: List<Gas>

	// Theres probably a better way of doing this
	override fun canTake(transportType: TransportType<*>, cache: LoadingCache<Sign, Int>, destinationSign: Sign): Int? {
		if (transportType !is GasTransport) return null

		if (!storableGasses.contains(transportType.gas)) return null

		val notPresent = !destinationSign.persistentDataContainer.keys.contains(transportType.gas.namespacedKey)
		val storedGasses = getStoredGasses(destinationSign).size

		if (notPresent && storedGasses >= maxGasTypes) return null

		return super.canTake(transportType, cache, destinationSign)
	}

	fun getStoredGasses(sign: Sign): Map<Gas, Int> {
		val map = mutableMapOf<Gas, Int>()
		val pdc = sign.persistentDataContainer

		for ((_, gas) in Gasses.all()) {
			val value = pdc.get(gas.namespacedKey, PersistentDataType.INTEGER) ?: continue

			map[gas] = value
		}

		return  map
	}
}
