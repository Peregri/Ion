package net.horizonsend.ion.server.features.tutorial.message

import org.bukkit.entity.Player

class TutorialAction(val delay: Double = 0.0, private val action: (Player) -> Unit) : TutorialMessage(delay) {
	override fun show(player: Player) {
		action(player)
	}
}
