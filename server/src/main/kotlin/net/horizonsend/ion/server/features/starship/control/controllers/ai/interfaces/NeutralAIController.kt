package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

/**
 * Neutral in the minecraft sense.
 * A controller that will switch to an aggressive controller
 **/
interface NeutralAIController : AggressiveLevelAIController {
	val starship: ActiveStarship

	fun createCombatController(controller: AIController, target: AITarget): AIController

	fun combatMode(controller: AIController, target: AITarget) {
		val combatMode = createCombatController(controller, target)

		starship.controller = combatMode
	}
}