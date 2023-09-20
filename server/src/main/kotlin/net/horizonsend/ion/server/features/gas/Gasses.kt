package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_EMPTY
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

@Suppress("UNUSED")
object Gasses : IonServerComponent(false) {
	private val gasses = mutableMapOf<String, Gas>()

	// Fuels
	val HYDROGEN = registerGas(
		object : GasFuel(
			identifier = "HYDROGEN",
			displayName = text("Hydrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_HYDROGEN",
			powerPerUnit = IonServer.gasConfiguration.gasses.hydrogen.powerPerUnit,
			cooldown = IonServer.gasConfiguration.gasses.hydrogen.cooldown,
			namespacedKey = NamespacedKeys.HYDROGEN,
			factorSupplier = IonServer.gasConfiguration.gasses.hydrogen::formattedFactors
		) {}
	)
	val NITROGEN = registerGas(
		object : GasFuel(
			identifier = "NITROGEN",
			displayName = text("Nitrogen", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_NITROGEN",
			powerPerUnit = IonServer.gasConfiguration.gasses.nitrogen.powerPerUnit,
			cooldown = IonServer.gasConfiguration.gasses.nitrogen.cooldown,
			namespacedKey = NamespacedKeys.NITROGEN,
			factorSupplier = IonServer.gasConfiguration.gasses.nitrogen::formattedFactors
		) {}
	)
	val METHANE = registerGas(
		object : GasFuel(
			identifier = "METHANE",
			displayName = text("Methane", NamedTextColor.RED),
			containerIdentifier = "GAS_CANISTER_METHANE",
			powerPerUnit = IonServer.gasConfiguration.gasses.methane.powerPerUnit,
			cooldown = IonServer.gasConfiguration.gasses.methane.cooldown,
			namespacedKey = NamespacedKeys.METHANE,
			factorSupplier = IonServer.gasConfiguration.gasses.methane::formattedFactors
		) {}
	)

	// Oxidizers
	val OXYGEN = registerGas(
		object : GasOxidizer(
			identifier = "OXYGEN",
			displayName = text("Oxygen", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_OXYGEN",
			powerMultipler = IonServer.gasConfiguration.gasses.oxygen.powerMultiplier,
			namespacedKey = NamespacedKeys.OXYGEN,
			factorSupplier = IonServer.gasConfiguration.gasses.oxygen::formattedFactors
		) {}
	)
	val CHLORINE = registerGas(
		object : GasOxidizer(
			identifier = "CHLORINE",
			displayName = text("Chlorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_CHLORINE",
			powerMultipler = IonServer.gasConfiguration.gasses.chlorine.powerMultiplier,
			namespacedKey = NamespacedKeys.CHLORINE,
			factorSupplier = IonServer.gasConfiguration.gasses.chlorine::formattedFactors
		) {}
	)
	val FLUORINE = registerGas(
		object : GasOxidizer(
			identifier = "FLUORINE",
			displayName = text("Fluorine", NamedTextColor.YELLOW),
			containerIdentifier = "GAS_CANISTER_FLUORINE",
			powerMultipler = IonServer.gasConfiguration.gasses.fluorine.powerMultiplier,
			namespacedKey = NamespacedKeys.FLUORINE,
			factorSupplier = IonServer.gasConfiguration.gasses.fluorine::formattedFactors
		) {}
	)

	// Other
	val HELIUM = registerGas(
		object : Gas(
			identifier = "HELIUM",
			displayName = text("Helium", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_HELIUM",
			namespacedKey = NamespacedKeys.HELIUM,
			factorSupplier = IonServer.gasConfiguration.gasses.helium::formattedFactors
		) {}
	)
	val CARBON_DIOXIDE = registerGas(
		object : Gas(
			identifier = "CARBON_DIOXIDE",
			displayName = text("Carbon Dioxide", NamedTextColor.BLUE),
			containerIdentifier = "GAS_CANISTER_CARBON_DIOXIDE",
			namespacedKey = NamespacedKeys.CARBON_DIOXIDE,
			factorSupplier = IonServer.gasConfiguration.gasses.carbonDioxide::formattedFactors
		) {}
	)

	private fun <T: Gas> registerGas(gas: T): T {
		gasses[gas.identifier] = gas
		return gas
	}

	val EMPTY_CANISTER: ItemStack = GAS_CANISTER_EMPTY.constructItemStack()

	fun isEmptyCanister(itemStack: ItemStack?): Boolean {
		return itemStack?.customItem?.identifier == GAS_CANISTER_EMPTY.identifier
	}

	fun isCanister(itemStack: ItemStack?): Boolean = isEmptyCanister(itemStack) || itemStack?.customItem is GasCanister

	fun findGas(location: Location) = gasses.values.filter { it.tryCollect(location) }
	fun findAvailableGasses(location: Location) = gasses.values.filter {
		it.canBeFound(location)
	}

	operator fun get(identifier: String) = gasses[identifier]

	operator fun get(itemStack: ItemStack?): Gas? {
		if (itemStack == null) return null

		val customItem = itemStack.customItem ?: return  null

		if (customItem !is GasCanister) return null

		return gasses[customItem.gasIdentifier]!!
	}

	operator fun get(key: NamespacedKey): Gas? {
		return gasses.values.firstOrNull { it.namespacedKey == key }
	}

	fun all() = gasses
}
