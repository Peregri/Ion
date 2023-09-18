package net.horizonsend.ion.server.features.multiblock.powerbank

import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.multiblock.GasStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import org.bukkit.Material

object GasTankMultiblock : Multiblock(), GasStoringMultiblock {
	val tierMaterial: Material = Material.DIAMOND_BLOCK
	override val name = "gastank"

	override val maxStoredValue = 500_000
	override val storableGasses: List<Gas> = Gasses.all().values.toList()

	override val signText = createSignText(
		line1 = "&cGas &7Tank",
		line2 = "&e------",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).extractor()
				x(+0).type(Material.SMITHING_TABLE)
				x(+1).extractor()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			for (i in -1..1) {
				y(i) {
					x(-1).anyGlass()
					x(+0).redstoneBlock()
					x(+1).anyGlass()
				}
			}
		}

		z(+2) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}
	}
}

