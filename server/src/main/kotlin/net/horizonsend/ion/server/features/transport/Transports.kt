package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.transport.type.GasTransport
import net.horizonsend.ion.server.features.transport.type.Power
import net.horizonsend.ion.server.features.transport.type.TransportType
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.metrics
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Transports : IonServerComponent() {
	lateinit var thread: ExecutorService
	val computerCheckQueue = ConcurrentLinkedQueue<() -> Unit>()

	val transportTypes = mutableListOf<TransportType<*>>(
		Power,
		GasTransport(Gasses.HYDROGEN, 2),
		GasTransport(Gasses.NITROGEN, 2),
		GasTransport(Gasses.METHANE, 2),
		GasTransport(Gasses.OXYGEN, 3),
		GasTransport(Gasses.CHLORINE, 3),
		GasTransport(Gasses.FLUORINE, 3),
		GasTransport(Gasses.HELIUM, 3),
		GasTransport(Gasses.CARBON_DIOXIDE, 3)
	)

	override fun onEnable() {
		metrics?.metricsManager?.registerCollection(IonMetricsCollection)
		thread = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("sl-transport-wires"))

		transportTypes.forEach {
			it.scheduleUpdates()
		}
	}

	override fun onDisable() {
		if (::thread.isInitialized) thread.shutdown()
	}

	fun getDirectionalRotation(data: BlockData): BlockFace = (data as Directional).facing
}
