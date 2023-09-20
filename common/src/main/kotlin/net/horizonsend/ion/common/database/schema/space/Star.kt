package net.horizonsend.ion.common.database.schema.space

import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

data class Star(
	override val _id: Oid<Star> = objId(),
	var name: String,
	var spaceWorld: String,
	var x: Int,
	var y: Int,
	var z: Int,
	var size: Double,
	val seed: Long,
	val layers: List<CrustLayer> = listOf(),
) : DbObject {
	companion object : OidDbObjectCompanion<Star>(Star::class, setup = {
		ensureUniqueIndex(Star::name)
	}) {
		fun create(name: String, spaceWorld: String, x: Int, y: Int, z: Int, size: Double, seed: Long): Oid<Star> {
			val id = objId<Star>()

			col.insertOne(
				Star(
					_id = id,
					name = name,
					spaceWorld = spaceWorld,
					x = x,
					y = y,
					z = z,
					size = size,
					seed = seed
				)
			)

			return id
		}

		fun setPos(id: Oid<Star>, spaceWorld: String, x: Int, y: Int, z: Int) {
			updateById(id, set(Star::spaceWorld setTo spaceWorld, Star::x setTo x, Star::y setTo y, Star::z setTo z))
		}

		fun setLayers(id: Oid<Star>, layers: List<CrustLayer>) {
			updateById(id, setValue(Star::layers, layers))
		}

		fun setLayer(id: Oid<Star>, layer: Int, newLayer: CrustLayer) {
			val existingLayers = (col[id] ?: return).layers.toMutableList()

			// Check if the list has an element at that position
			if ((existingLayers.size - 1) >= layer) {
				existingLayers[layer] = newLayer
			} else {
				existingLayers.add(newLayer)
			}

			setLayers(id, existingLayers)
		}

		fun setSize(id: Oid<Star>, size: Double): UpdateResult =
			col.updateOneById(id, setValue(Star::size, size))
	}

	data class CrustLayer(
		val separation: Int,
		val noise: Double,
		val materials: List<String>
	)
}
