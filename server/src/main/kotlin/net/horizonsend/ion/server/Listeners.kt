package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.HyperspaceBeaconManager
import net.horizonsend.ion.server.features.achievements.AchievementListeners
import net.horizonsend.ion.server.features.customItems.blasters.listeners.PlayerInteractListener
import net.horizonsend.ion.server.features.customItems.blasters.listeners.PlayerItemHoldListener
import net.horizonsend.ion.server.features.customItems.blasters.listeners.PlayerItemSwapListener
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.listeners.EntityDamageListener
import net.horizonsend.ion.server.listeners.PlayerDeathListener
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.listeners.PlayerLoginListener
import net.horizonsend.ion.server.listeners.PlayerQuitListener
import net.horizonsend.ion.server.listeners.PlayerResourcePackStatusListener
import net.horizonsend.ion.server.listeners.WorldInitListener
import net.horizonsend.ion.server.listeners.WorldUnloadListener
import net.horizonsend.ion.server.listeners.eventcancelling.BlockFadeListener
import net.horizonsend.ion.server.listeners.eventcancelling.BlockFormListener
import net.horizonsend.ion.server.listeners.eventcancelling.PlayerFishListener
import net.horizonsend.ion.server.listeners.eventcancelling.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.eventcancelling.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.eventcancelling.PotionSplashListener
import net.horizonsend.ion.server.listeners.gameplaytweaks.EnchantItemListener
import net.horizonsend.ion.server.listeners.gameplaytweaks.PrepareItemCraftListener
import net.horizonsend.ion.server.listeners.gameplaytweaks.PrepareItemEnchantListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryMoveItemListener

val listeners = arrayOf(
	BlockFadeListener(),
	BlockFormListener(),
	ChunkLoadListener(Ion),
	EnchantItemListener(),
	EntityDamageListener(),
	HyperspaceBeaconManager,
	InventoryClickListener(),
	InventoryCloseListener(),
	InventoryDragListener(),
	InventoryMoveItemListener(),

	PlayerDeathListener(), // Head Drop on death

	PlayerDeathListener(),
	PlayerItemSwapListener(),
	PlayerFishListener(),
	PlayerItemConsumeListener(),
	PlayerItemHoldListener(),
	PlayerInteractListener(),
	PlayerJoinListener(),
	PlayerLoginListener(),
	PlayerQuitListener(),
	PlayerResourcePackStatusListener(),
	PlayerTeleportListener(),
	PotionSplashListener(),
	PrepareItemCraftListener(),
	PrepareItemEnchantListener(),
	WorldInitListener(),
	WorldUnloadListener(),

	// Achievement Listeners
	AchievementListeners()
)
