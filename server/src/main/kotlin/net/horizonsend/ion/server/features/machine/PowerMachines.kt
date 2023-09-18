package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.transport.type.Power
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack

object PowerMachines : IonServerComponent() {
	override fun onEnable() {
		// IIRC the below is a hacky fix for generators, it should be removed if possible, moved if not

		val deadBush = ItemStack(Material.DEAD_BUSH)
		if (Bukkit.getRecipesFor(deadBush).size == 0) {
			val key = NamespacedKey(IonServer, "focusing_crystal")
			val recipe = FurnaceRecipe(key, deadBush, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		// Another hacky fix that should be removed to make gas power plants work
		val bone = ItemStack(Material.BONE)
		if (Bukkit.getRecipesFor(bone).size == 0) {
			val key = NamespacedKey(IonServer, "gas_canisters")
			val recipe = FurnaceRecipe(key, deadBush, Material.WARPED_FUNGUS_ON_A_STICK, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		val yellowFlower = ItemStack(Material.DANDELION)
		if (Bukkit.getRecipesFor(yellowFlower).size == 0) {
			val key = NamespacedKey(IonServer, "dud")
			val recipe = FurnaceRecipe(key, yellowFlower, Material.SNOWBALL, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}
	}

	@JvmOverloads
	fun setPower(sign: Sign, power: Int, fast: Boolean = true): Int = Power.setStoredValue(sign, power, fast)

	@JvmOverloads
	fun getPower(sign: Sign, fast: Boolean = true): Int = Power.getStoredValue(sign, fast)

	fun addPower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) + amount)
	}

	fun removePower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) - amount, true)
	}
}
