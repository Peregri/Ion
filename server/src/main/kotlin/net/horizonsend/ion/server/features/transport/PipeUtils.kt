package net.horizonsend.ion.server.features.transport

import net.horizonsend.ion.server.miscellaneous.utils.MATERIALS
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlassPane
import org.bukkit.Material
import java.util.EnumMap

val colorMap = EnumMap(
	MATERIALS.filter { it.isGlass || it.isGlassPane }.associateWith {
		return@associateWith when {
			it == Material.GLASS_PANE -> Material.GLASS
			it.isStainedGlassPane -> Material.getMaterial(it.name.removeSuffix("_PANE"))!!
			else -> it
		}
	}
)

fun isDirectionalPipe(material: Material): Boolean = material.isGlassPane

fun isColoredPipe(material: Material): Boolean = material.isStainedGlass || material.isStainedGlassPane
