package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector

class VisualParticleProjectile private constructor(
	location: Location,
	dir: Vector,
	override val range: Double,
	override val speed: Double,
	override val soundName: String,
	override val thickness: Double,
) : ParticleProjectile(starship = null, loc = location, dir = dir, shooter = null) {
	constructor(
		location: Location,
		dir: Vector,
		range: Double,
		speed: Double,
		soundName: String,
		thickness: Double,
		dustOptions: DustOptions
	) : this(location, dir, range, speed, soundName, thickness) {
		this.dustOptions = dustOptions
	}

	constructor(
		location: Location,
		dir: Vector,
		range: Double,
		speed: Double,
		soundName: String,
		thickness: Double,
		particle: Particle
	) : this(location, dir, range, speed, soundName, thickness) {
		this.particle = particle
	}

	private var dustOptions: DustOptions? = null
	private var particle: Particle? = null

	override val explosionPower: Float = 0f

	override val shieldDamageMultiplier: Int = 0

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		dustOptions?.let {
			val particle = Particle.REDSTONE
			loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, it, force)
		}

		particle?.let {
			loc.world.spawnParticle(it, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, force)
		}
	}
}
