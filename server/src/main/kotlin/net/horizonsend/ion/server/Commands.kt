package net.horizonsend.ion.server

import net.horizonsend.ion.server.commands.ConfigurationCommands
import net.horizonsend.ion.server.commands.ConvertCommand
import net.horizonsend.ion.server.commands.CustomItemCommand
import net.horizonsend.ion.server.commands.SettingsCommand
import net.horizonsend.ion.server.bounties.commands.BountyCommands
import net.horizonsend.ion.server.commands.UtilityCommands
import net.horizonsend.ion.server.achievements.commands.AchievementsCommand

val commands = arrayOf(
	BountyCommands(),
	ConfigurationCommands(),
	ConvertCommand(),
	CustomItemCommand(),
	SettingsCommand(),
	UtilityCommands(),

	AchievementsCommand()
)
