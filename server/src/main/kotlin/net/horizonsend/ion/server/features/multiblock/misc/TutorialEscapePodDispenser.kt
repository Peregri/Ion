package net.horizonsend.ion.server.features.multiblock.misc

import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

class TutorialEscapePodDispenser : Multiblock(), InteractableMultiblock {
	override val name: String = "escapepod"

	override val signText: Array<Component?> = arrayOf(
		null,
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		TODO("Not yet implemented")
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		TODO("Not yet implemented")
	}

	override fun setupSign(player: Player, sign: Sign) {
		if (!player.hasPermission("group.dutymode")) return

		super.setupSign(player, sign)
	}
}
