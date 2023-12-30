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
			// Check for ecumenical keys in bank or inventory
			Item[] items = event.getItemContainer().getItems();
			int tempKeyCount = 0;
			for (Item item : items) {
				if (item.getId() == ItemID.ECUMENICAL_KEY) {
					tempKeyCount += item.getQuantity();
				}
			}
			// Store key count
			if (event.getContainerId() == InventoryID.INVENTORY.getId())  {
				log.debug("Keys in inventory: " + tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountInventory", tempKeyCount);
			} else {
				log.debug("Keys in bank: " + tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountBank", tempKeyCount);
			}
		}
	}

	public String generateOverlayText() {
		StringBuilder message = new StringBuilder();

		// Get current key count
		String inventoryCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountInventory");
		String bankCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "ecumenicalKeyCountBank");

		if (bankCountStr == null || inventoryCountStr == null) {
			return null;
		}

		int inventoryCount = Integer.parseInt(inventoryCountStr);
		int bankCount = Integer.parseInt(bankCountStr);
		message.append(inventoryCount + bankCount);

		// Max key limit
		int maxAmount = 3;
		if (client.getVarbitValue(DIARY_WILDERNESS_HARD) == 1) {
			maxAmount = 5;
		} else if (client.getVarbitValue(DIARY_WILDERNESS_MEDIUM) == 1) {
			maxAmount = 4;
		}
		message.append("/").append(maxAmount);

		// Return tooltip
		message.append(" keys collected");

		return message.toString();
	}
}
