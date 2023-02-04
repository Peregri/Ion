package net.horizonsend.ion.server.explosionreversal

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.explosionreversal.Regeneration.pulse
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldSaveEvent
import java.io.IOException
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.roundToLong

object ExplosionReversalPlugin : Listener {
    var settings: Settings? = null
        private set
    var worldData: WorldData? = null
        private set

	fun onEnable() {
        loadConfigAndUpdateDefaults()
        initializeWorldData()
        scheduleRegen()
    }

    private fun initializeWorldData() {
        worldData = WorldData()
    }

    private fun scheduleRegen() {
        val scheduler = Bukkit.getServer().scheduler
        scheduler.runTaskTimer(IonServer.Ion, Runnable {
            try {
                pulse(this)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, 5L, 5L)
    }

    @EventHandler
    fun onWorldSave(event: WorldSaveEvent) {
        worldData!!.save(event.world)
    }

	fun onDisable() {
        saveAll()
    }

    private fun saveAll() {
        Bukkit.getWorlds().forEach(Consumer { world: World? ->
            worldData!!.save(
                world!!
            )
        })
    }

    private fun loadConfigAndUpdateDefaults() {
        IonServer.Ion.saveDefaultConfig()
        settings = Settings(IonServer.Ion.config)
    }

    fun getExplodedTime(
        explosionX: Double, explosionY: Double, explosionZ: Double,
        blockX: Int, blockY: Int, blockZ: Int
    ): Long {
        val now = System.currentTimeMillis()
        val distance = abs(explosionX - blockX) + abs(explosionY - blockY) + abs(explosionZ - blockZ)
        val distanceDelayMs = settings!!.distanceDelay * 1000
        val cap = settings!!.getDistanceDelayCap()
        val offset = (cap.coerceAtMost(cap - distance) * distanceDelayMs).roundToLong()
        return now + offset
    }
}
