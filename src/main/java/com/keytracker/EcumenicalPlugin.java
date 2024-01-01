package com.keytracker;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;

import static net.runelite.api.Varbits.DIARY_WILDERNESS_MEDIUM;
import static net.runelite.api.Varbits.DIARY_WILDERNESS_HARD;

@Slf4j
@PluginDescriptor(
	name = "Ecumenical Key Tracker"
)
public class EcumenicalPlugin extends Plugin {
	private static final String CONFIG_GROUP_NAME = "ecumenical-key-tracker";
	private static final String CONFIG_KEY_INV = "ecumenicalKeyCountInventory";
	private static final String CONFIG_KEY_BANK = "ecumenicalKeyCountBank";
	private static final String CONFIG_SHARD_INV = "ecumenicalShardCountInventory";
	private static final String CONFIG_SHARD_BANK = "ecumenicalShardCountBank";

	private static final int WILDERNESS_GOD_WARS_DUNGEON_REGION_ID = 12190;

	private static final int SHARD_PER_KEY = 50;
	private static final int DIARY_NO_AMOUNT = 3;
	private static final int DIARY_MEDIUM_AMOUNT = 4;
	private static final int DIARY_HARD_AMOUNT = 5;

	private InfoBox ecumenicalInfoBox;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EcumenicalOverlay overlay;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Override
	public void startUp() {
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown() {
		overlayManager.remove(overlay);
		if (ecumenicalInfoBox != null) {
			infoBoxManager.removeInfoBox(ecumenicalInfoBox);
		}
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
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_INV, tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_INV, tempShardCount);
			} else {
				log.debug("Keys in bank: " + tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_BANK, tempKeyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_BANK, tempShardCount);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		switch (gameStateChanged.getGameState()) {
			case LOADING:
				if (isInWildernessGodWarsDungeon()) {
					ecumenicalInfoBox = generateInfoBox();
					infoBoxManager.addInfoBox(ecumenicalInfoBox);
				} else {
					infoBoxManager.removeInfoBox(ecumenicalInfoBox);
				}
				break;
			case HOPPING:
			case LOGIN_SCREEN:
				if (ecumenicalInfoBox != null) {
					infoBoxManager.removeInfoBox(ecumenicalInfoBox);
				}
				break;
		}
	}

	private boolean isInWildernessGodWarsDungeon() {
		return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), WILDERNESS_GOD_WARS_DUNGEON_REGION_ID);
	}

	private int getMaxKeyAmount() {
		if (client.getVarbitValue(DIARY_WILDERNESS_HARD) == 1) {
			return DIARY_HARD_AMOUNT;
		} else if (client.getVarbitValue(DIARY_WILDERNESS_MEDIUM) == 1) {
			return DIARY_MEDIUM_AMOUNT;
		}
		return DIARY_NO_AMOUNT;
	}

	private int shardsToKeys(int shards) {
		return shards / SHARD_PER_KEY;
	}

	public String generateInfoMessage() {
		StringBuilder message = new StringBuilder();

		// Get current key count
		// TODO Dont get (and check) values from config every frame
		String inventoryCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_INV);
		String bankCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_BANK);

		// Get current shard count
		String inventoryShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_INV);
		String bankShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_BANK);

		// Check if bank has been opened
		if (bankCountStr == null || inventoryCountStr == null) {
			return null;
		}

		// Get totals as int
		int totalKeyCount = Integer.parseInt(inventoryCountStr) + Integer.parseInt(bankCountStr);
		int totalShardCount = Integer.parseInt(inventoryShardCountStr) + Integer.parseInt(bankShardCountStr);

		// Max key limit
		int maxAmount = getMaxKeyAmount();

		// Make tooltip message
		message.append(totalKeyCount).append("/").append(maxAmount).append(" keys");
		if (totalShardCount != 0) {
			message.append("</br>").append(totalShardCount).append(" (").append(shardsToKeys(totalShardCount)).append(") shards");
		}

		return message.toString();
	}

	private InfoBox generateInfoBox() {
		return new InfoBox(itemManager.getImage(ItemID.ECUMENICAL_KEY), this) {
			@Override
			public String getText() {
				return null;
			}

			@Override
			public Color getTextColor() {
				return Color.WHITE;
			}

			@Override
			public String getTooltip() {
				return generateInfoMessage();
			}
		};
	}
}
