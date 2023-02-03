package net.horizonsend.ion.server.registrations

import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.NamespacedKey

object Keys {
	val AMMO = key("Ammo")
	val CUSTOM_ITEM = key("CustomItem")
	val MULTIBLOCK = key("multiblock")
	val ORE_CHECK = key("oreCheck")
	val POWER = key("power")

	fun key(key: String) = NamespacedKey(Ion, key)
}
