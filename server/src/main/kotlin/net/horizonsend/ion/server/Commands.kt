package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.ConfigurationCommands
import net.horizonsend.ion.server.commands.ConvertCommand
import net.horizonsend.ion.server.commands.CustomItemCommand
import net.horizonsend.ion.server.commands.SettingsCommand
import net.horizonsend.ion.server.commands.UtilityCommands
import net.horizonsend.ion.server.features.achievements.AchievementsCommand
import net.horizonsend.ion.server.features.bounties.BountyCommands

val commands = arrayOf(
	BountyCommands(),
	ConfigurationCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	UtilityCommands(),

	AchievementsCommand()
)
