package net.horizonsend.ion.server.explosionreversal

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors

class Settings internal constructor(config: FileConfiguration) {
    /**
     * Time in minutes after explosion blocks should regenerate
     */
    val regenDelay: Double

    /**
     * Time in seconds of additional delay for each block away (taxicab distance)
     */
    val distanceDelay: Double

    /**
     * Maximum distance to consider
     */
    private val distanceDelayCap: Int

    /**
     * Maximum time in milliseconds that should be spent per tick regenerating exploded blocks
     */
    val placementIntensity: Double

    /**
     * Worlds to ignore explosions in
     */
    val ignoredWorlds: Set<String>

    /**
     * Entities to ignore the explosions of
     */
    val ignoredEntityExplosions: Set<EntityType?>

    /**
     * Entities to not regenerate
     */
    val ignoredEntities: Set<EntityType?>

    /**
     * Additional types of entities to regenerate.
     */
    val includedEntities: Set<EntityType?>

    /**
     * Types of blocks to not regenerate
     */
    val ignoredMaterials: Set<Material?>

    /**
     * Types of blocks to regenerate. When empty, include all blocks
     */
    val includedMaterials: Set<Material?>

    init {
        regenDelay = config.getDouble("regen_delay", 5.0)
        distanceDelay = config.getDouble("distance_delay", 2.0)
        distanceDelayCap = config.getInt("distance_delay_cap", 6)
        placementIntensity = config.getDouble("placement_intensity", 5.0)
        ignoredWorlds = getStringSet(config, "ignored_worlds")
        ignoredEntityExplosions = getEntityTypes(config, "ignored_entity_explosions")
        ignoredEntities = getEntityTypes(config, "ignored_entities")
        ignoredMaterials = getMaterials(config, "ignored_materials")
        includedMaterials = getMaterials(config, "included_materials")
        includedEntities = getEntityTypes(config, "included_entities")
    }

    private fun getStringSet(config: FileConfiguration, path: String): Set<String> {
        return HashSet(config.getStringList(path))
    }

    private fun getEntityTypes(config: FileConfiguration, path: String): Set<EntityType?> {
        return getStringSet(config, path).stream()
            .map { string: String -> parseEntityType(string) }
            .filter { obj: EntityType? -> Objects.nonNull(obj) }
            .collect(Collectors.toSet())
    }

    private fun getMaterials(config: FileConfiguration, path: String): Set<Material?> {
        return getStringSet(config, path).stream()
            .map { string: String -> parseMaterial(string) }
            .filter { obj: Material? -> Objects.nonNull(obj) }
            .collect(Collectors.toSet())
    }

    private fun parseEntityType(string: String): EntityType? {
        return try {
            EntityType.valueOf(string.uppercase(Locale.getDefault()))
        } catch (exception: Exception) {
            log.severe(
                "Failed to parse entity type " + string + "! Make sure it is a valid entity type. " +
                        "Valid entity types can be viewed at https://papermc.io/javadocs/org/bukkit/entity/EntityType.html"
            )
            null
        }
    }

    private fun parseMaterial(string: String): Material? {
        return try {
            val material = Material.valueOf(string)
            if (!material.isBlock) {
                log.severe("$material is not a block!")
                return null
            }
            material
        } catch (exception: Exception) {
            log.severe(
                "Failed to parse material " + string + "! Make sure it is a valid material. " +
                        "Valid materials can be viewed at https://papermc.io/javadocs/org/bukkit/Material.html"
            )
            null
        }
    }

    fun getDistanceDelayCap(): Double {
        return distanceDelayCap.toDouble()
    }

    companion object {
        private val log = Logger.getLogger(Settings::class.java.name)
    }
}
