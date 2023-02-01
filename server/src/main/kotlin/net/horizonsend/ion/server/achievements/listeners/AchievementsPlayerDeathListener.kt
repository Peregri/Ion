package net.horizonsend.ion.server.achievements.listeners

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class AchievementsPlayerDeathListener : Listener {
	@EventHandler
	fun onPlayerDeathEvent(event: PlayerDeathEvent){
		val killer = event.entity.killer ?: return // Only player kills
		val victim = event.player

		if (killer !== victim) killer.rewardAchievement(Achievement.KILL_PLAYER) // Kill a Player Achievement
	}
}
