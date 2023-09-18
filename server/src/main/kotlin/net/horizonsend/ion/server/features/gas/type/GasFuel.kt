package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import java.util.function.Supplier

abstract class GasFuel(
	identifier: String,
	displayName: Component,
	containerIdentifier: String,
	namespacedKey: NamespacedKey,
	factorSupplier: Supplier<List<CollectionFactor>>,

	val powerPerUnit: Int,
	val cooldown: Int
) : Gas(identifier, displayName, containerIdentifier, namespacedKey, factorSupplier)

