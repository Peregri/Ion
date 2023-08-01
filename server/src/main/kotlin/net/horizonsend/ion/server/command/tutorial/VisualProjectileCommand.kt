package net.horizonsend.ion.server.command.tutorial

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VisualParticleProjectile
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector

@CommandAlias("visualprojectile")
@CommandPermission("ion.visualprojectile")
object VisualProjectileCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onVisualProjectile(
		worldName: String,
		x1: Double,
		y1: Double,
		z1: Double,
		x2: Double,
		y2: Double,
		z2: Double,
		range: Double,
		speed: Double,
		particleName: String,
		thickness: Double,
		soundName: String
	) {
		val world = Bukkit.getWorld(worldName) ?: return
		val start = Vector(x1, y1, z1)
		val end = Vector(x2, y2, z2)

		val dir = end.subtract(start)
		val loc = start.toLocation(world)

		val particle = Particle.valueOf(particleName)

		VisualParticleProjectile(
			loc,
			dir,
			range,
			speed,
			soundName,
			thickness,
			particle
		).fire()
	}

	@Default
	@Suppress("unused")
	fun onVisualProjectile(
		worldName: String,
		x1: Double,
		y1: Double,
		z1: Double,
		x2: Double,
		y2: Double,
		z2: Double,
		range: Double,
		speed: Double,
		r: Int,
		b: Int,
		g: Int,
		thickness: Double,
		soundName: String
	) {
		val world = Bukkit.getWorld(worldName) ?: return
		val color = Color.fromRGB(r, b, g)

		val start = Vector(x1, y1, z1)
		val end = Vector(x2, y2, z2)

		val dir = end.subtract(start)
		val loc = start.toLocation(world)

		val dustOptions = DustOptions(color, thickness.toFloat())

		VisualParticleProjectile(
			loc,
			dir,
			range,
			speed,
			soundName,
			thickness,
			dustOptions
		).fire()
	}
}
