package net.horizonsend.ion.server.features.multiblock.gas

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.GasStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.transport.Transports
import net.horizonsend.ion.server.features.transport.type.GasTransport
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.random.Random

object DirectGasCollectorMulitblock : Multiblock(), FurnaceMultiblock, GasStoringMultiblock, InteractableMultiblock {
	override val name = "gascollector"

	override val signText = createSignText(
		line1 = "&cGas &6Collector",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val maxStoredValue: Int = 1000
	override val storableGasses: Set<Gas> = Gasses.all().values.toSet()

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).craftingTable()
				x(0).type(Material.SMITHING_TABLE)
				x(+1).craftingTable()
			}
			y(0) {
				x(-1).lightningRod()
				x(0).machineFurnace()
				x(+1).lightningRod()
			}
		}
		z(1) {
			y(-1) {
				x(-1).ironBlock()
				x(0).lightningRod()
				x(+1).ironBlock()
			}
			y(0) {
				x(-1).lightningRod()
				x(0).copperBlock()
				x(+1).lightningRod()
			}
		}
		z(2) {
			y(-1) {
				x(-1).anyStairs()
				x(0).lightningRod()
				x(+1).anyStairs()
			}
			y(0) {
				x(-1).lightningRod()
				x(0).copperBlock()
				x(+1).lightningRod()
			}
		}
	}

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		event.isCancelled = true

		println(1)

		if (furnace.inventory.fuel?.type != Material.PRISMARINE_CRYSTALS) return
		println(2)
		if (furnace.inventory.smelting?.type != Material.PRISMARINE_CRYSTALS) return

		event.isBurning = false
		event.burnTime = (50 + Math.random() * 100).toInt()
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		tickCollector(sign)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val available = Gasses.findAvailableGasses(sign.location).joinToString { it.identifier }

		player.information("Available gasses: $available")
	}

	private fun tickCollector(collector: Sign) = Tasks.async {
		val attachedFace = collector.getFacing().oppositeFace

		val world = collector.world
		println(3)
		if (!world.isChunkLoaded((collector.x + attachedFace.modX) shr 4, (collector.z + attachedFace.modZ) shr 4)) return@async

		println(4)
		val furnace = collector.block.getRelativeIfLoaded(attachedFace) ?: return@async
		val location = furnace.location

		println(5)
		val availableGasses = Gasses.findGas(location)
		val gas = availableGasses.shuffled().firstOrNull { it.tryCollect(location) } ?: return@async
		println(6)

		val amount = 30 + Random.nextInt(0, 30)

		harvestGas(collector, gas, amount)
	}

	private fun harvestGas(collector: Sign, gas: Gas, amount: Int) {
		val existingGas = getTransportType(collector, gas)

		Tasks.sync { existingGas.addValue(collector, amount) }
	}

	private fun getTransportType(sign: Sign, gas: Gas): GasTransport {
		val pdc = sign.persistentDataContainer.keys

		val existingGasses: List<GasTransport> = Transports.transportTypes.filter {
			it is GasTransport && pdc.contains(it.namespacedKey)
		} as List<GasTransport>

		val first = existingGasses.firstOrNull()

		// Deal with the possibility
		if (existingGasses.size > 1) {
			val existing = existingGasses.firstOrNull { it.gas == gas }

			// Remove all gasses that aren't the existing, or the first, since it will be the returned one
			pdc.removeAll { Gasses[it] != null && it != existing?.namespacedKey && it != first?.namespacedKey }
			IonServer.logger.warning("$this at ${Vec3i(sign.location)} has multiple gasses! ${existingGasses.joinToString { it.gas.identifier }}")

			Tasks.sync { sign.update() }
		}

		// Return the existing gas, or if there are none, the transport type of the provided gas.
		return first ?: GasTransport[gas] ?: throw NotImplementedError("Gas ${gas.identifier} doesn't have a transport type!")
	}
}
