package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import java.util.function.Supplier

abstract class GasOxidizer(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	namespacedKey: NamespacedKey,
	factorSupplier: Supplier<List<CollectionFactor>>,

	val powerMultipler: Double
) : Gas(identifier, displayName, containerIdentifier, namespacedKey, factorSupplier)

