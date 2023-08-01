package net.horizonsend.ion.server.command.tutorial

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.util.Vector

@CommandAlias("visualprojectile")
class VisualProjectileCommand : SLCommand() {
	@Default
	fun onVisualProjectile(
		x1: Double,
		y1: Double,
		z1: Double,
		x2: Double,
		y2: Double,
		z2: Double,
		speed: Double,
	) {
		val start = Vector(x1, y1, z1)
		val end = Vector(x2, y2, z2)

		val dir = end.subtract(start)
	}
}
