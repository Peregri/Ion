package net.horizonsend.ion.server.features.starship.mininglaser

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblock
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblockTier1
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblockTier2
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblockTier3
import net.horizonsend.ion.server.miscellaneous.extensions.alert
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.drills.DrillMultiblock
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import net.starlegacy.util.toLocation
import org.bukkit.Bukkit.getPlayer
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class MiningLaserSubsystem(
	override val starship: ActivePlayerStarship,
	pos: Vec3i,
	private val face: BlockFace,
	val multiblock: MiningLaserMultiblock
) : WeaponSubsystem(starship, pos), ManualWeaponSubsystem {
	private val firingTasks = mutableListOf<BukkitTask>()
	var isFiring = false
	lateinit var targetedBlock: Vector

	private val DISABLED = Component.text("[DISABLED]").color(NamedTextColor.RED)

	override val powerUsage: Int = 0
	private val blockBreakPowerUsage: Double = 0.5
	private val radiusSquared = multiblock.mineRadius * multiblock.mineRadius

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val firePos = getFirePos()
		val vector = target.clone().subtract(firePos.toVector())
		val default = firePos.toVector().add(vector.clone().normalize().multiply(multiblock.range))

		return default
// 		return starship.serverLevel.world.rayTrace(
// 			firePos.toLocation(starship.serverLevel.world),
// 			vector.clone(),
// 			multiblock.range,
// 			FluidCollisionMode.NEVER,
// 			true,
// 			0.1,
// 			null
// 		)?.hitPosition ?: default
	}

	override fun getMaxPerShot(): Int? {
		if (multiblock is MiningLaserMultiblockTier1 && starship.type.canMine && starship.initialBlockCount <= StarshipType.SHUTTLE.maxSize) {
			return 1
		}
		if (multiblock is MiningLaserMultiblockTier2 && starship.type.canMine) {
			if (starship.initialBlockCount in 1000..2000){
				return 1
			}
			if (starship.initialBlockCount in 2000..4000){
				return 2
			}
		}
		if (multiblock is MiningLaserMultiblockTier3 && starship.type.canMine) {
			if (starship.initialBlockCount in 4000..8000){
				return 4
			}
			if (starship.initialBlockCount in 8000..12000){
				return 6
			}
		}
		return null
	}

	private fun setUser(sign: Sign, player: String?) {
		val line3 = player?.let { Component.text(player) } ?: DISABLED
		sign.line(3, line3)
		sign.update(false, false)
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	private fun getFirePos(): Vec3i {
		val (x, y, z) = multiblock.getFirePointOffset()
		val facing = getSign()?.getFacing() ?: face
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}

	private fun getSign() = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	// TODO use this for the multiple guardian beams
	private fun getPoints(axis: Vector): List<Location> {
		val spread: Double = 360.0 / multiblock.beamCount
		val points = mutableListOf<Location>()
		val start = axis.clone().normalize().rotateAroundZ(90.0).multiply(multiblock.mineRadius).add(axis.clone())

		for (count in multiblock.beamCount.downTo(1)) {
			val newLoc = start.rotateAroundNonUnitAxis(axis.clone(), spread * count)
			points.add(newLoc.toLocation(starship.serverLevel.world))
		}

		return points
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector) {
		val sign = getSign() ?: return

		setFiring(!isFiring, sign, shooter)
		this.targetedBlock = target.clone()
	}

	private fun setFiring(firing: Boolean, sign: Sign, user: Player? = null) {
		isFiring = firing

		when (firing) {
			true -> {
				setUser(sign, user!!.name)
				starship.information("Enabled mining laser at $pos")
				startFiringSequence()
			}

			false -> {
				setUser(sign, null)
				starship.information("Disabled mining laser at $pos")
				cancelTask()
			}
		}
	}

	private fun startFiringSequence() {
		val fireTask = object : BukkitRunnable() {
			override fun run() {
				if (isFiring) {
					fire()
				} else {
					cancel()
				}
			}
		}.runTaskTimer(IonServer, 0L, 5L)

		starship.world.playSound(multiblock.getFirePointOffset().toLocation(starship.world), "starship.weapon.mining_laser.mining_laser_start", 1f, 1f)

		firingTasks.add(fireTask)
	}

	private fun cancelTask() {
		isFiring = false
		firingTasks.forEach { it.cancel() }
		firingTasks.clear()
		starship.centerOfMass.toLocation(starship.world).world.players.forEach {
			if (it.location.distance(starship.centerOfMass.toLocation(starship.world)) < multiblock.range) {
				starship.centerOfMass.toLocation(starship.world).world.playSound(it.location, "starship.weapon.mining_laser.mining_laser_stop", 1.0f, 1f)
			}
		}
	}

	fun fire() {
		if (!ActiveStarships.isActive(starship)) {
			cancelTask()
			return
		}

		val sign = getSign() ?: return
		val user = getPlayer((sign.line(3) as TextComponent).content()) ?: return cancelTask()
		val power = PowerMachines.getPower(sign, true)

		if (power == 0) {
			starship.alert("Drill at ${sign.block.x}, ${sign.block.y}, ${sign.block.z} ran out of power and was disabled!")

			setFiring(false, sign)
			return
		}

		val initialPos = getFirePos().toLocation(starship.serverLevel.world).toCenterLocation().add(pos.toVector())
		val targetVector = targetedBlock.clone().subtract(initialPos.toVector())

		// TODO rewrite all of the aiming stuff

		val raytrace = starship.serverLevel.world.rayTrace(
			initialPos,
			targetVector.clone(),
			multiblock.range,
			FluidCollisionMode.NEVER,
			true,
			0.1,
			null
		)

		targetedBlock = raytrace?.hitPosition ?: targetedBlock

		val laserEnd = raytrace?.hitBlock?.location ?: targetedBlock.toLocation(starship.serverLevel.world)
		val laser = CrystalLaser(initialPos, laserEnd, 5, -1).durationInTicks()
		laser.start(IonServer)

		val block = laserEnd.block
		val blocks = getBlocksToDestroy(block)

		if (
			DrillMultiblock.breakBlocks(
				sign = sign,
				maxBroken = multiblock.maxBroken,
				toDestroy = blocks,
				output = multiblock.getOutput(sign),
				isDrillMultiblock = false,
				people = starship.passengerIDs.mapNotNull(::getPlayer).toTypedArray(),
				player = user
			) > 0
		) {
			PowerMachines.setPower(sign, power - (blockBreakPowerUsage * blocks.size).toInt(), true)
			sign.world.playSound(laserEnd, multiblock.sound, 1f, 1f)
		}

		laserEnd.world.spawnParticle(Particle.EXPLOSION_HUGE, laserEnd, 1)
	}

	private fun getBlocksToDestroy(center: Block): MutableList<Block> {
		val toDestroy = mutableListOf<Block>()

		val range = IntRange(-multiblock.mineRadius, multiblock.mineRadius)

		for (x in range) {
			val xSquared = x * x

			for (y in range) {
				val ySquared = y * y

				for (z in range) {
					val zSquared = z * z

					if ((xSquared + ySquared + zSquared) >= radiusSquared) continue

					val toExplode = center.getRelative(BlockFace.EAST, x)
						.getRelative(BlockFace.UP, y)
						.getRelative(BlockFace.SOUTH, z)

					if (toExplode.type == Material.AIR) continue
					if (toExplode.type == Material.BEDROCK) continue
					if (toExplode.type == Material.REINFORCED_DEEPSLATE) continue

					toDestroy.add(toExplode)
				}
			}
		}

		toDestroy.sortBy { it.location.distanceSquared(pos.toLocation(center.world)) }

		return toDestroy
	}
}