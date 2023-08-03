package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.execConsoleCmd
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.UUID
import kotlin.collections.set

object TutorialManager : IonServerComponent() {
	private var playersInTutorials = mutableMapOf<Player, TutorialInstance>()
	private var readTimes = mutableMapOf<UUID, Long>()

	private const val WORLD_NAME = "Tutorial"

	private const val SEPERATION_DISTANCE = 2000
	private const val MAX_SEPERATIONS = 4

	private val tutorialOrigin = Vec3i(0, 111, 0)

	private fun getWorld(): World = Bukkit.getWorld(WORLD_NAME) ?: error("Tutorial world not found")

	fun isWorld(world: World): Boolean = world.name == WORLD_NAME

	override fun onEnable() {
		listen<PlayerJoinEvent> { event ->
			val player = event.player

			player.resetTitle()
			playersInTutorials.remove(player) // who knows...

			if (isWorld(player.world)) teleportToStart(player)
		}

		listen<PlayerQuitEvent> { event ->
			stop(event.player)
		}

		listen<BlockBreakEvent> { event ->
			if (isWorld(event.block.world)) {
				event.isCancelled = true
			}
		}

//		listen<StarshipRotateEvent> { event ->
//			val player = event.player
//			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.TURN_RIGHT) {
//				event.isCancelled = true
//			}
//		}
//
//		listen<StarshipStartCruisingEvent> { event ->
//			val player = event.player
//			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.CRUISE_START) {
//				event.isCancelled = true
//			}
//		}
//
//		listen<StarshipTranslateEvent> { event ->
//			val player = event.player
//			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.SHIFT_FLY_FORWARD
//			) {
//				event.isCancelled = true
//			}
//		}

		// disable all damage in the world
		listen<EntityDamageEvent> { event ->
			if (isWorld(event.entity.world)) {
				event.isCancelled = true
			}
		}

		// erase chunks in the world
		listen<ChunkUnloadEvent> { event ->
			if (!isWorld(event.world)) {
				return@listen
			}

			val chunk = event.chunk

			val datas = DeactivatedPlayerStarships.getInChunk(chunk)
			if (datas.any()) {
				log.warn("Deleting " + datas.size + " starship computers in tutorial world")
				return@listen
			}

			Tasks.sync {
				for (data in datas) {
					StarshipDestruction.vanish(data)
				}
			}
		}

		listen<StarshipUnpilotEvent>(priority = EventPriority.LOW) { event ->
			val player = event.player

			if (!isWorld(player.world) || playersInTutorials[player]?.phase == TutorialPhase.LAST) {
				return@listen
			}

			event.isCancelled = true

			player.userError("Please wait until the intro is over to release your starship")
//
//			stop(player)
//
//			StarshipDestruction.vanish(event.starship)
//			event.isCancelled = true
//
//			Tasks.syncDelay(10) {
//				player title Title.builder()
//					.title(red("Tutorial Canceled"))
//					.subtitle(gray("Unpiloted (right clicked computer) before the tutorial end"))
//					.fadeIn(10)
//					.stay(40)
//					.fadeOut(10)
//					.build()
//			}
		}

		// if someone places a ship computer in an existing one, overwrite it
		listen<BlockPlaceEvent>(priority = EventPriority.LOWEST) { event ->
			if (isWorld(event.block.world) && event.block.type == Material.JUKEBOX) {
				val loc = event.block.location
				DeactivatedPlayerStarships[loc.world, loc.blockX, loc.blockY, loc.blockZ]?.let { state ->
					log.warn("Deleted computer ${loc.world.name}@${Vec3i(loc)} because someone placed over it")
					DeactivatedPlayerStarships.destroyAsync(state)
				}
			}
		}

		Tasks.sync {
			TutorialPhase.values().forEach(TutorialPhase::setupHandlers)
		}
	}

	fun start(player: Player) {
		require(PilotedStarships[player] == null)

		try { playersInTutorials[player] = createTutorialInstance(player) } catch (error: NotImplementedError) {
			for (onlinePlayer in IonServer.server.onlinePlayers) {
				if (!player.hasPermission("group.helper")) continue

				player.serverError("TUTORIAL FULL!")
			}
		}

		val loc = Location(
			Bukkit.getWorld("Tutorial"),
			0.0,
			0.0,
			0.0
		)

//		loadShip(loc)
		player.teleport(loc)
		player.teleport(loc) // teleport a second time, because, well... minecraft

		startPhase(player, TutorialPhase.FIRST)
	}

	fun startPhase(player: Player, phase: TutorialPhase) {
		require(playersInTutorials.containsKey(player))

		playersInTutorials[player]?.phase = phase

		phase.onStart(player)

		var time = 0L
		for ((index, message) in phase.messages.withIndex()) {
			Tasks.syncDelay(time) {
				if (getPhase(player) == phase) {
					message.show(player)
				}
			}

			if (index == phase.messages.lastIndex) {
				break
			}

			time += (message.seconds * 20).toInt()
		}

		val uuid = player.uniqueId

		Tasks.syncDelay(time + 1) { readTimes.remove(uuid) }

		// add 1 second since this is just in case it doesn't get removed automatically somehow
		readTimes[uuid] = System.currentTimeMillis() + time * 50L + 1000L // 50 ms per tick
	}

	fun stop(player: Player) {
		readTimes.remove(player.uniqueId)

		val instance: TutorialInstance? = playersInTutorials.remove(player)

		if (!isWorld(player.world)) {
			return
		}

		player.resetTitle()

		Tasks.syncDelay(10) {
			when (instance?.phase) {
				TutorialPhase.LAST -> teleportToEnd(player)
				else -> teleportToStart(player)
			}
		}

		return
	}

	fun createTutorialInstance(player: Player): TutorialInstance {
		for (x in -MAX_SEPERATIONS .. MAX_SEPERATIONS) for (z in -MAX_SEPERATIONS .. MAX_SEPERATIONS) {
			val occupied = playersInTutorials.any { (_, instance) -> instance.xSeperations == x && instance.zSeperations == z }

			if (occupied) continue

			return TutorialInstance(
				player,
				x,
				z,
				TutorialPhase.FIRST
			)
		}

		// tutorial is full
		throw NotImplementedError()
	}

	private fun teleportToStart(player: Player) {
		execConsoleCmd("warp tutorialstart ${player.name}")
	}

	private fun teleportToEnd(player: Player) {
		execConsoleCmd("warp tutorialend ${player.name}")
	}

	fun isReading(player: Player): Boolean = (readTimes[player.uniqueId] ?: 0L) >= System.currentTimeMillis()

	fun getPhase(player: Player): TutorialPhase? = playersInTutorials[player]?.phase

	data class TutorialInstance(
		val player: Player,
		val xSeperations: Int,
		val zSeperations: Int,
		var phase: TutorialPhase
	) {
		fun origin(): Vec3i {
			val (oldX, y, oldZ) = tutorialOrigin

			val xOffset = xSeperations * SEPERATION_DISTANCE
			val zOffset = zSeperations * SEPERATION_DISTANCE

			val newX = oldX + xOffset
			val newZ = oldZ + zOffset

			return Vec3i(newX, y, newZ)
		}
	}


}
