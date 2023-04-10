package net.starlegacy.feature.economy.city

enum class TradeCityType(
	val protection: Boolean,
	val bazaar: Boolean,
	val crates: Boolean,
	val upkeep: Double,
	val minActive: Int,
	val npc: Boolean,
	val crateChance: Double
) {
	NPC(true, true, true, 0.0, 0, true, 0.1),
	SETTLEMENT(false,false,false,0.0, 0, false, 0.0),
	TIERONE(false, true, false, 75.0, 2, false, 0.15),
	TIERTWO(false, true, true, 1525.0, 2, false, 0.25),
	TIERTHREE(true, true, true, 200.0, 2, false,.035),
}
