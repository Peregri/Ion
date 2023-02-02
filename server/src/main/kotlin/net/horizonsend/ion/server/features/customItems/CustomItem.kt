package net.horizonsend.ion.server.features.customItems

import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

abstract class CustomItem(val identifier: String) {
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	abstract fun constructItemStack(): ItemStack
}
