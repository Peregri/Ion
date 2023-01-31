package net.horizonsend.ion.server.explosionreversal.listener

import net.horizonsend.ion.server.explosionreversal.ExplosionReversalPlugin
import net.horizonsend.ion.server.explosionreversal.data.ExplodedBlockData
import net.horizonsend.ion.server.explosionreversal.nms.NMSUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.type.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.InventoryHolder
import java.io.IOException
import java.util.LinkedList
import java.util.Objects

class ExplosionListener(private val plugin: ExplosionReversalPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Throws(
        IOException::class
    )
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (plugin.settings!!.ignoredEntityExplosions.contains(event.entity.type)) {
            return
        }
        val world = event.entity.world
        val location = event.location
        val blockList = event.blockList()
        processExplosion(world, location, blockList)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Throws(
        IOException::class
    )
    fun onBlockExplode(event: BlockExplodeEvent) {
        val world = event.block.world
        val location = event.block.location
        val blockList = event.blockList()
        processExplosion(world, location, blockList)
    }

    @Throws(IOException::class)
    private fun processExplosion(world: World, explosionLocation: Location, list: MutableList<Block>) {
        if (plugin.settings!!.ignoredWorlds.contains(world.name)) {
            return
        }
        if (list.isEmpty()) {
            return
        }
        val explodedBlockDataList: MutableList<ExplodedBlockData> = LinkedList()
        val eX = explosionLocation.x
        val eY = explosionLocation.y
        val eZ = explosionLocation.z
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            processBlock(explodedBlockDataList, eX, eY, eZ, iterator)
        }

        // if no blocks were handled by the plugin at all (for example, every block's type is ignored)
        if (explodedBlockDataList.isEmpty()) {
            return
        }
        plugin.worldData!!.addAll(world, explodedBlockDataList)
    }

    @Throws(IOException::class)
    private fun processBlock(
        explodedBlockDataList: MutableList<ExplodedBlockData>, eX: Double, eY: Double, eZ: Double,
        iterator: MutableIterator<Block>
    ) {
        val block = iterator.next()
        val blockData = block.blockData
        if (ignoreMaterial(blockData.material)) {
            return
        }
        val x = block.x
        val y = block.y
        val z = block.z
        val explodedTime = plugin.getExplodedTime(eX, eY, eZ, x, y, z)
        val tileEntity = NMSUtils.getTileEntity(block)
        if (tileEntity != null) {
            processTileEntity(explodedBlockDataList, block, explodedTime)
        }
        val explodedBlockData = ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntity)
        explodedBlockDataList.add(explodedBlockData)

        // break the block manually
        iterator.remove()
        block.setType(Material.AIR, false)
    }

    private fun ignoreMaterial(material: Material): Boolean {
        val settings = plugin.settings
        val includedMaterials = settings!!.includedMaterials
        return material == Material.AIR ||
                settings.ignoredMaterials.contains(material) || includedMaterials.isNotEmpty() && !includedMaterials.contains(
            material
        )
    }

    @Throws(IOException::class)
    private fun processTileEntity(
        explodedBlockDataList: MutableList<ExplodedBlockData>,
        block: Block,
        explodedTime: Long
    ) {
        val state = block.state
        if (state is InventoryHolder) {
            val inventory = (state as InventoryHolder).inventory
            // Double chests are weird so you have to get the state (as a holder)'s inventory's holder to cast to DoubleChest
            val inventoryHolder = inventory.holder
            if (inventoryHolder is DoubleChest) {
                val isRight = (block.blockData as Chest).type == Chest.Type.RIGHT
                processDoubleChest(explodedBlockDataList, isRight, inventoryHolder, explodedTime)
            }
            inventory.clear()
        }
    }

    @Throws(IOException::class)
    private fun processDoubleChest(
        explodedBlockDataList: MutableList<ExplodedBlockData>, isRight: Boolean,
        doubleChest: DoubleChest, explodedTime: Long
    ) {
        val inventory = doubleChest.inventory as DoubleChestInventory
        val otherInventory = if (isRight) inventory.rightSide else inventory.leftSide
        val otherInventoryLocation = Objects.requireNonNull(otherInventory.location)
        val other = otherInventoryLocation!!.block
        val otherX = other.x
        val otherY = other.y
        val otherZ = other.z
        val otherBlockData = other.blockData
        val otherTile = NMSUtils.getTileEntity(other)
        explodedBlockDataList.add(ExplodedBlockData(otherX, otherY, otherZ, explodedTime, otherBlockData, otherTile))
        otherInventory.clear()
        other.setType(Material.AIR, false)
    }
}
