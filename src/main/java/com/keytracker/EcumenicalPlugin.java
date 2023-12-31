package com.keytracker;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.api.Varbits.DIARY_WILDERNESS_MEDIUM;
import static net.runelite.api.Varbits.DIARY_WILDERNESS_HARD;

@Slf4j
@PluginDescriptor(
	name = "Ecumenical Key Tracker"
)
public class EcumenicalPlugin extends Plugin {
	public static final String CONFIG_GROUP_NAME = "ecumenical-key-tracker";

	public static final int SHARD_PER_KEY = 50;
	public static final int DIARY_NO_AMOUNT = 3;
	public static final int DIARY_MEDIUM_AMOUNT = 4;
	public static final int DIARY_HARD_AMOUNT = 5;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EcumenicalOverlay overlay;

	@Override
	public void startUp() {
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown() {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId() || event.getContainerId() == InventoryID.BANK.getId()) {
			// Check for ecumenical keys/shards in bank or inventory
			Item[] items = event.getItemContainer().getItems();
			int tempKeyCount = 0;
			int tempShardCount = 0;
			for (Item item : items) {
				if (item.getId() == ItemID.ECUMENICAL_KEY) {
					tempKeyCount += item.getQuantity();
				} else if (item.getId() == ItemID.ECUMENICAL_KEY_SHARD) {
					tempShardCount += item.getQuantity();
				}
			}
			// Store amount
			if (event.getContainerId() == InventoryID.INVENTORY.getId())  {
				log.debug("Keys in inventory: " + tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountInventory", tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalShardCountInventory", tempShardCount);
			} else {
				log.debug("Keys in bank: " + tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountBank", tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalShardCountBank", tempShardCount);
			}
		}
	}

	public String generateOverlayText() {
		StringBuilder message = new StringBuilder();

		// Get current key count
		String inventoryCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountInventory");
		String bankCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountBank");

		// Get current shard count
		String inventoryShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalShardCountInventory");
		String bankShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalShardCountBank");

		// Check if bank has been opened
		if (bankCountStr == null || inventoryCountStr == null) {
			return null;
		}

		// Get totals as int
		int totalKeyCount = Integer.parseInt(inventoryCountStr) + Integer.parseInt(bankCountStr);
		int totalShardCount = Integer.parseInt(inventoryShardCountStr) + Integer.parseInt(bankShardCountStr);

		// Max key limit
		int maxAmount = DIARY_NO_AMOUNT;
		if (client.getVarbitValue(DIARY_WILDERNESS_HARD) == 1) {
			maxAmount = DIARY_HARD_AMOUNT;
		} else if (client.getVarbitValue(DIARY_WILDERNESS_MEDIUM) == 1) {
			maxAmount = DIARY_MEDIUM_AMOUNT;
		}

		// Make tooltip message
		message.append(totalKeyCount).append("/").append(maxAmount).append(" keys");
		if (totalShardCount != 0) {
			int shardAsKeys = totalShardCount / SHARD_PER_KEY;
			message.append("</br>").append(totalShardCount).append(" (").append(shardAsKeys).append(") shards");
		}

		return message.toString();
	}
}
