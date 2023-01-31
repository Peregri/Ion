package net.horizonsend.ion.server.explosionreversal

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.horizonsend.ion.server.explosionreversal.data.ExplodedBlockData
import net.horizonsend.ion.server.explosionreversal.data.ExplodedEntityData
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.EntityType
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.LinkedList
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WorldData {
    private val explodedBlocks = CacheBuilder.newBuilder()
        .weakKeys()
        .build(
            CacheLoader.from { world: World -> loadBlocks(world) }
		)
    private val explodedEntities = CacheBuilder.newBuilder()
        .weakKeys()
        .build(
            CacheLoader.from { world: World -> loadEntities(world) }
		)

    private fun getBlocksFile(world: World): File {
        return File(world.worldFolder, "data/explosion_regen_blocks.dat")
    }

    private fun getEntitiesFile(world: World): File {
        return File(world.worldFolder, "data/explosion_regen_entities.dat")
    }

    fun getBlocks(world: World): MutableList<ExplodedBlockData> {
        return explodedBlocks.getUnchecked(world)
    }

    fun getEntities(world: World): MutableList<ExplodedEntityData> {
        return explodedEntities.getUnchecked(world)
    }

    fun save(world: World) {
        saveBlocks(world)
        saveEntities(world)
    }

    private fun loadBlocks(world: World): MutableList<ExplodedBlockData> {
        val file = getBlocksFile(world)
        if (!file.exists()) {
            return LinkedList()
        }
        val blocks: MutableList<ExplodedBlockData> = LinkedList()
        try {
            DataInputStream(FileInputStream(file)).use { input ->
                val palette = readPalette(input)
                readBlocks(blocks, input, palette)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            file.renameTo(File(file.parentFile, file.name + "_broken_" + System.currentTimeMillis() % 1000))
            saveBlocks(world)
        }
        return blocks
    }

    @Throws(IOException::class)
    private fun readPalette(input: DataInputStream): List<BlockData> {
        val paletteSize = input.readInt()
        val palette: MutableList<BlockData> = ArrayList()
        for (i in 0 until paletteSize) {
            val blockDataBytes = ByteArray(input.readInt())
            input.read(blockDataBytes)
            val blockDataString = String(blockDataBytes)
            palette.add(Bukkit.createBlockData(blockDataString))
        }
        return palette
    }

    @Throws(IOException::class)
    private fun readBlocks(blocks: MutableList<ExplodedBlockData>, input: DataInputStream, palette: List<BlockData>) {
        val blockCount = input.readInt()
        for (i in 0 until blockCount) {
            val x = input.readInt()
            val y = input.readInt()
            val z = input.readInt()
            val explodedTime = input.readLong()
            val blockData = palette[input.readInt()]
            var tileEntityData: ByteArray? = null
            if (input.readBoolean()) {
                val tileEntitySize = input.readInt()
                tileEntityData = ByteArray(tileEntitySize)
                input.read(tileEntityData)
            }
            blocks.add(ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntityData))
        }
    }

    private fun saveBlocks(world: World) {
        val file = getBlocksFile(world)
        file.parentFile.mkdirs()
        val tmpFile = File(file.parentFile, file.name + "_tmp")
        try {
            DataOutputStream(FileOutputStream(tmpFile)).use { output ->
                val data: List<ExplodedBlockData> = getBlocks(world)
                val palette = writePalette(output, data)
                writeBlocks(output, data, palette)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tmpFile.delete()
            return
        }
        try {
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun writePalette(output: DataOutputStream, data: List<ExplodedBlockData>): Map<BlockData, Int> {
        val palette =
            data.stream().map { obj: ExplodedBlockData -> obj.blockData }.distinct().collect(Collectors.toList())
        val values: MutableMap<BlockData, Int> = HashMap(palette.size)
        output.writeInt(palette.size)
        for (i in palette.indices) {
            val value = palette[i]
            val valueString = value.getAsString(true)
            output.writeInt(valueString.length)
            output.writeBytes(valueString)
            values[value] = i
        }
        return values
    }

    @Throws(IOException::class)
    private fun writeBlocks(output: DataOutputStream, data: List<ExplodedBlockData>, palette: Map<BlockData, Int>) {
        output.writeInt(data.size)
        for (block in data) {
            output.writeInt(block.x)
            output.writeInt(block.y)
            output.writeInt(block.z)
            output.writeLong(block.explodedTime)
            output.writeInt(palette[block.blockData]!!)
            val tileData = block.tileData
            if (tileData == null) {
                output.writeBoolean(false)
            } else {
                output.writeBoolean(true)
                output.writeInt(tileData.size)
                output.write(tileData)
            }
        }
    }

    private fun loadEntities(world: World): MutableList<ExplodedEntityData> {
        val file = getEntitiesFile(world)
        if (!file.exists()) {
            return LinkedList()
        }
        val entities: MutableList<ExplodedEntityData> = LinkedList()
        try {
            DataInputStream(FileInputStream(file)).use { input ->
                val entityCount = input.readInt()
                for (i in 0 until entityCount) {
                    val explodedEntityData = readEntityData(input)
                    entities.add(explodedEntityData)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            file.renameTo(File(file.parentFile, file.name + "_broken_" + System.currentTimeMillis() % 1000))
            saveEntities(world)
        }
        return entities
    }

    @Throws(IOException::class)
    private fun readEntityData(input: DataInputStream): ExplodedEntityData {
        val entityType = EntityType.values()[input.readInt()]
        val x = input.readDouble()
        val y = input.readDouble()
        val z = input.readDouble()
        val pitch = input.readFloat()
        val yaw = input.readFloat()
        val explodedTime = input.readLong()
        var entityData: ByteArray? = null
        if (input.readBoolean()) {
            val entityDataSize = input.readInt()
            entityData = ByteArray(entityDataSize)
            input.read(entityData)
        }
        return ExplodedEntityData(entityType, x, y, z, pitch, yaw, explodedTime, entityData)
    }

    private fun saveEntities(world: World) {
        val file = getEntitiesFile(world)
        file.parentFile.mkdirs()
        val tmpFile = File(file.parentFile, file.name + "_tmp")
        try {
            DataOutputStream(FileOutputStream(tmpFile)).use { output ->
                val data: List<ExplodedEntityData> = getEntities(world)
                output.writeInt(data.size)
                for (entity in data) {
                    writeEntity(output, entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tmpFile.delete()
            return
        }
        try {
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun writeEntity(output: DataOutputStream, entity: ExplodedEntityData) {
        output.writeInt(entity.entityType.ordinal)
        output.writeDouble(entity.x)
        output.writeDouble(entity.y)
        output.writeDouble(entity.z)
        output.writeFloat(entity.pitch)
        output.writeFloat(entity.yaw)
        output.writeLong(entity.explodedTime)
        val entityData = entity.nmsData
        if (entityData == null) {
            output.writeBoolean(false)
        } else {
            output.writeBoolean(true)
            output.writeInt(entityData.size)
            output.write(entityData)
        }
    }

    fun addAll(world: World, explodedBlockData: Collection<ExplodedBlockData>?) {
        getBlocks(world).addAll(explodedBlockData!!)
    }

    fun addEntity(world: World, explodedEntityData: ExplodedEntityData) {
        getEntities(world).add(explodedEntityData)
    }
}
