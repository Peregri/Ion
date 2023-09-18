package net.horizonsend.ion.server.features.multiblock.generator

import org.bukkit.Material

object GeneratorMultiblockTier3 : GeneratorMultiblock("&bTier 3", Material.DIAMOND_BLOCK) {
	override val speed = 1.5
	override val maxStoredValue = 250_000
}
