package net.horizonsend.ion.server.features.multiblock.gas

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object GasCollectorMultiblock : Multiblock(), FurnaceMultiblock, InteractableMultiblock {
	override val name = "gascollector"

	override val signText = createSignText(
		line1 = "&cGas &6Collector",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).machineFurnace()
		at(0, 0, 1).hopper()
	}

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		event.isCancelled = true
		val smelting = furnace.inventory.smelting

		if (smelting == null || smelting.type != Material.PRISMARINE_CRYSTALS) return

		if (!Gasses.isCanister(furnace.inventory.fuel)) return

		event.isBurning = false
		event.burnTime = (500 + Math.random() * 250).toInt()
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
		if (!world.isChunkLoaded((collector.x + attachedFace.modX) shr 4, (collector.z + attachedFace.modZ) shr 4)) return@async

		val furnace = collector.block.getRelativeIfLoaded(attachedFace) ?: return@async
		val hopper = furnace.getRelativeIfLoaded(attachedFace) ?: return@async

		for (face in arrayOf(
			attachedFace.rightFace,
			attachedFace.leftFace,
			BlockFace.UP,
			BlockFace.DOWN
		)) {
			val lightningRod = furnace.getRelativeIfLoaded(face) ?: continue
			if (lightningRod.type != Material.LIGHTNING_ROD) continue
			val blockFace = (lightningRod.blockData as Directional).facing
			if (blockFace != face && blockFace != face.oppositeFace) continue

			val location = lightningRod.getRelativeIfLoaded(face)?.location ?: continue
			val availableGasses = Gasses.findGas(location)

			val gas = availableGasses.shuffled().firstOrNull { it.tryCollect(location) } ?: return@async

			Tasks.sync {
				val result = tryHarvestGas(furnace, hopper, gas)
				val sound = if (result) Sound.ITEM_BOTTLE_FILL_DRAGONBREATH else Sound.ITEM_BOTTLE_FILL
				lightningRod.world.playSound(lightningRod.location, sound, 10.0f, 0.5f)
			}
		}
	}

	fun tryHarvestGas(furnaceBlock: Block, hopperBlock: Block, gas: Gas): Boolean {
		val furnace = furnaceBlock.getState(false) as Furnace
		val hopper = hopperBlock.getState(false) as Hopper

		val canisterItem = furnace.inventory.fuel ?: return false
		val customItem = canisterItem.customItem ?: return false

		return when (customItem) {
			CustomItems.GAS_CANISTER_EMPTY -> fillEmptyCanister(furnace, gas)

			is GasCanister -> fillGasCanister(canisterItem, furnace, hopper) // Don't even bother with the gas

			else -> false
		}
	}

	private fun fillEmptyCanister(furnace: Furnace, gas: Gas): Boolean {
		val newType = CustomItems.getByIdentifier(gas.containerIdentifier) as? GasCanister ?: return false
		val newCanister = newType.createWithFill(IonServer.gasConfiguration.collectorAmount)

		furnace.inventory.fuel = newCanister

		return true
	}

	private fun fillGasCanister(canisterItem: ItemStack, furnace: Furnace, hopper: Hopper): Boolean {
		val type = canisterItem.customItem ?: return false
		if (type !is GasCanister) return  false

		val currentFill = type.getFill(canisterItem)
		val newFill = currentFill + IonServer.gasConfiguration.collectorAmount

		// If the canister would be filled
		return if (newFill >= type.maximumFill) {
			// Try to add a full canister to the hopper
			val canAdd = hopper.inventory.addItem(type.constructItemStack())

			// If it can be added
			if (canAdd.isEmpty()) {
				// Clear it from the furnace
				furnace.inventory.fuel = null
			} else {
				// Put a full one in its spot
				furnace.inventory.fuel = type.constructItemStack()

				return false
			}

			true
		} else {
			// If it's completely not filled, just fill it to the new level
			type.setFill(canisterItem, newFill)

			true
		}
	}
}
