package net.horizonsend.ion.server.registrations

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

val vaultEconomy = try {
	Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
} catch (exception: NoClassDefFoundError) {
	null
}
