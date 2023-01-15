package net.horizonsend.ion.server.starships.subcraft

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.HSVLike
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.schema.starships.SubCraftData
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.event.StarshipComputerOpenMenuEvent
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.toBlockPos
import org.bukkit.Material
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object SubShipComputers : Listener {
	val COMPUTER_TYPE = Material.LOOM

	@EventHandler
	fun onInteract(event: PlayerInteractEvent) {
		val player = event.player
		val block = event.clickedBlock ?: return

		if (event.hand != EquipmentSlot.HAND) {
			return // it can fire with both hands
		}

		if (block.type != COMPUTER_TYPE) {
			return
		}

		if (!StarshipControl.isHoldingController(player)) {
			player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Not holding starship controller, ignoring computer click")
			return
		}

		event.isCancelled = true
		val data: SubCraftData? = SubShipComputers[block.x, block.y, block.z]

		if (data == null) {
			player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Sub-ships are detected alongside the parent craft!")
			return
		}

		when (event.action) {
			Action.LEFT_CLICK_BLOCK -> tryOpenMenu(player, data)
			else -> return
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block
		// get a DEACTIVATED computer. Using StarshipComputers#get would include activated ones
		val subShipData = SubCraftData.findByKey(block.location.toBlockPos().asLong()).first() ?: return
		val parentData = PlayerStarshipData.findById(subShipData.parent) ?: return
		val parentPos = BlockPos.of(parentData.blockKey)
		val parent = DeactivatedPlayerStarships[parentData.bukkitWorld(), parentPos.x, parentPos.y, parentPos.z] ?: return

		val player = event.player
		DeactivatedPlayerStarships.removeSubShipAsync(parent, subShipData) {
			player.sendFeedbackActionMessage(FeedbackType.SUCCESS, "Destroyed sub-ship starship computer")
		}
	}

	operator fun get(x: Int, y: Int, z: Int): SubCraftData? {
		return SubCraftData.findByKey(BlockPos.asLong(x, y, z)).first()
	}

	fun SubCraftData.getParent(): PlayerStarshipData? {
		return PlayerStarshipData.findById(this.parent)
	}

	private fun tryOpenMenu(player: Player, data: SubCraftData) {
		val parent = data.getParent() ?: return // this should never happen

		if (!parent.isPilot(player) && !player.hasPermission("ion.core.starship.override")) {
			Tasks.async {
				val name: String? = SLPlayer.getName(parent.captain)
				if (name != null) {
					player.sendFeedbackActionMessage(
						FeedbackType.USER_ERROR,
						"You're not a pilot of this ship! The captain is {0}",
						name
					)
				}
			}
			return
		}

		if (!StarshipComputerOpenMenuEvent(player).callEvent()) {
			return
		}

		MenuHelper.apply {
			val pane = staticPane(0, 0, 9, 1)

			pane.addItem(
				guiButton(Material.NAME_TAG) {
					player.closeInventory()
					startRename(playerClicker, data)
				}.setName(MiniMessage.miniMessage().deserialize("<gray>Starship Name")),
				8, 0
			)

			// TODO

			pane.setOnClick { e ->
				e.isCancelled = true
			}

			gui(1, getDisplayName(data).replace("<[^>]*>".toRegex(), "")).withPane(pane).show(player)
		}
	}

	private fun startRename(player: Player, data: SubCraftData) {
		player.beginConversation(
			Conversation(
				IonServer.Ion, player,
				object : StringPrompt() {
					override fun getPromptText(context: ConversationContext): String {
						return "Enter new starship name:"
					}

					override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
						if (input != null) {
							Tasks.async {
								val serialized = MiniMessage.miniMessage().deserialize(input)

								if (serialized.clickEvent() != null ||
									input.contains("<rainbow>") ||
									input.contains("<newline>") ||
									serialized.hoverEvent() != null ||
									serialized.insertion() != null ||
									serialized.hasDecoration(TextDecoration.OBFUSCATED) ||
									((serialized as? TextComponent)?.content()?.length ?: 0) >= 16
								) {
									player.sendFeedbackMessage(FeedbackType.USER_ERROR, "ERROR: Disallowed tags!")
									return@async
								}

								if (serialized.color() != null && !player.hasPermission("ion.starship.color")) {
									player.sendFeedbackMessage(
										FeedbackType.USER_ERROR,
										"<COLOR> tags can only be used by $5+ patrons! Donate at\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								if ((serialized.color() as? HSVLike) != null && serialized.color()!!.asHSV().v() < 0.25) {
									player.sendFeedbackMessage(
										FeedbackType.USER_ERROR,
										"Ship names can't be too dark to read!"
									)
									return@async
								}

								if (
									serialized.decorations().any { it.value == TextDecoration.State.TRUE } &&
									!player.hasPermission("ion.starship.italic")
								) {
									player.sendFeedbackMessage(
										FeedbackType.USER_ERROR,
										"\\<italic>, \\<bold>, \\<strikethrough> and \\<underlined> tags can only be used by $10+ patrons!\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								if (serialized.font() != null && !player.hasPermission("ion.starship.font")) {
									player.sendFeedbackMessage(
										FeedbackType.USER_ERROR,
										"\\<font> tags can only be used by $15+ patrons! Donate at\n" +
											"Donate at https://www.patreon.com/horizonsendmc/ to receive this perk."
									)
									return@async
								}

								DeactivatedPlayerStarships.updateName(data, input)

								player.sendFeedbackMessage(FeedbackType.SUCCESS, "Changed starship name to $input.")
							}
						}
						return null
					}
				}
			)
		)
	}

	fun getDisplayName(data: SubCraftData): String {
		return data.name ?: "Sub-Ship"
	}
}
