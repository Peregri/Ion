package net.horizonsend.ion.server.registrations

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.customitems.CustomItemListeners
import net.horizonsend.ion.server.features.blasters.BlasterListeners
import net.horizonsend.ion.server.features.bounties.BountyListeners
import net.horizonsend.ion.server.features.worlds.WorldListeners
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.listeners.*
import net.horizonsend.ion.server.misc.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryMoveItemListener

val listeners = arrayOf(
	ChunkLoadListener(Ion),
	WorldListeners(),
	HyperspaceBeaconManager,
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),
	CancelListeners(),
	GameplayTweaksListeners(),
	ResourcePackListener(),
	MiscListeners(),
	CustomItemListeners(),
	BlasterListeners(),
	BountyListeners(),

	// Achievement Listeners
	AchievementListeners()
)
