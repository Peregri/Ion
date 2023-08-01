package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.subsystem.weapon.Projectiles
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

abstract class Projectile {
	open fun fire() {
		Tasks.syncDelay(0, ::tick)
	}

	protected abstract fun tick()

	protected fun reschedule() {
		Tasks.syncDelay(Projectiles.TICK_INTERVAL, ::tick)
	}
}
