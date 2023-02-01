package net.horizonsend.ion.server

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.achievements.listeners.AchievementsPlayerDeathListener
import net.horizonsend.ion.server.legacy.listeners.ChunkLoadListener
import net.horizonsend.ion.server.achievements.listeners.DetectShipListener
import net.horizonsend.ion.server.achievements.listeners.EnterPlanetListener
import net.horizonsend.ion.server.listeners.EventCancelling.BlockFadeListener
import net.horizonsend.ion.server.listeners.EventCancelling.BlockFormListener
import net.horizonsend.ion.server.listeners.GamePlayTweaks.EnchantItemListener
import net.horizonsend.ion.server.listeners.EntityDamageListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryClickListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryCloseListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryDragListener
import net.horizonsend.ion.server.misc.screens.listeners.InventoryMoveItemListener
import net.horizonsend.ion.server.achievements.listeners.PlayerAttemptPickupItemListener
import net.horizonsend.ion.server.listeners.PlayerDeathListener
import net.horizonsend.ion.server.listeners.EventCancelling.PlayerFishListener
import net.horizonsend.ion.server.listeners.EventCancelling.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.PlayerFishListener
import net.horizonsend.ion.server.listeners.PlayerInteractListener
import net.horizonsend.ion.server.listeners.PlayerItemConsumeListener
import net.horizonsend.ion.server.listeners.PlayerItemHoldListener
import net.horizonsend.ion.server.listeners.PlayerItemSwapListener
import net.horizonsend.ion.server.listeners.PlayerJoinListener
import net.horizonsend.ion.server.listeners.PlayerLoginListener
import net.horizonsend.ion.server.listeners.PlayerQuitListener
import net.horizonsend.ion.server.listeners.PlayerResourcePackStatusListener
import net.horizonsend.ion.server.listeners.EventCancelling.PlayerTeleportListener
import net.horizonsend.ion.server.listeners.EventCancelling.PotionSplashListener
import net.horizonsend.ion.server.listeners.GamePlayTweaks.PrepareItemCraftListener
import net.horizonsend.ion.server.listeners.GamePlayTweaks.PrepareItemEnchantListener
import net.horizonsend.ion.server.listeners.WorldInitListener
import net.horizonsend.ion.server.listeners.WorldUnloadListener
import net.horizonsend.ion.server.managers.HyperspaceBeaconManager

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

	PlayerAttemptPickupItemListener(),
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

	//Achievement Listeners
	DetectShipListener(),
	EnterPlanetListener(),
	PlayerAttemptPickupItemListener(),
	AchievementsPlayerDeathListener()
)
