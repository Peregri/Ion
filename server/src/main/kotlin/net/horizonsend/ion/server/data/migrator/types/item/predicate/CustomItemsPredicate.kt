package net.horizonsend.ion.server.data.migrator.types.item.predicate

import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import org.bukkit.inventory.ItemStack

class CustomItemsPredicate(vararg identifier: String) : ItemMigratorPredicate {
	private val identifiers = identifier.toList()
	override fun shouldMigrate(item: ItemStack): Boolean {
		return identifiers.contains(item.newCustomItem?.identifier ?: return false)
	}
}
