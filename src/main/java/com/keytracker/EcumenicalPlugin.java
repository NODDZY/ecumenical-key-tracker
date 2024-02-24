package com.keytracker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.Varbits.DIARY_WILDERNESS_HARD;
import static net.runelite.api.Varbits.DIARY_WILDERNESS_MEDIUM;

@Slf4j
@PluginDescriptor(
	name = "Ecumenical Key Tracker",
	description = "Tracks ecumenical keys in inventory and bank",
	tags = { "ecumenical", "key", "track", "gwd" }
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

	private int totalKeyCount;
	private int totalShardCount;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EcumenicalConfig ecumenicalConfig;

	@Inject
	private EcumenicalOverlay overlay;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Provides
	EcumenicalConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(EcumenicalConfig.class);
	}

	@Override
	public void startUp() {
		overlayManager.add(overlay);
		updateCounts();
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
			ItemContainer container = event.getItemContainer();
			int keyCount = container.count(ItemID.ECUMENICAL_KEY);
			int shardCount = container.count(ItemID.ECUMENICAL_KEY_SHARD);

			// Store amount using config file and in class variables
			if (event.getContainerId() == InventoryID.INVENTORY.getId())  {
				log.debug("In inventory: {} key(s), {} shard(s)", keyCount, shardCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_INV, keyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_INV, shardCount);
				updateCounts();
			} else {
				log.debug("Banked: {} key(s), {} shard(s)", keyCount, shardCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_BANK, keyCount);
				configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_BANK, shardCount);
				updateCounts();
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		switch (gameStateChanged.getGameState()) {
			case LOGGED_IN:
				displayInfobox();
				break;
			case CONNECTION_LOST:
			case HOPPING:
			case LOGIN_SCREEN:
				// Prevent duplicate infoboxes when hopping/logging
				if (ecumenicalInfoBox != null) {
					infoBoxManager.removeInfoBox(ecumenicalInfoBox);
				}
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getGroup().equals(EcumenicalConfig.CONFIG_GROUP)) {
			if (configChanged.getKey().equals("showShardCount")) {
				return;
			}

			if (client.getGameState().equals(GameState.LOGGED_IN)) {
				displayInfobox();
			}
		}
	}

	private void displayInfobox() {
		if (ecumenicalInfoBox != null) {
			infoBoxManager.removeInfoBox(ecumenicalInfoBox);
		}

		// Infobox is turned off
		if (!ecumenicalConfig.displayInfobox()) {
			return;
		}
		// Display infobox everywhere
		if (!ecumenicalConfig.onlyDisplayWhileInDungeon()) {
			ecumenicalInfoBox = generateInfoBox();
			infoBoxManager.addInfoBox(ecumenicalInfoBox);
			return;
		}
		// Display infobox only while inside the Wilderness God Wars Dungeon
		if (isInWildernessGodWarsDungeon()) {
			ecumenicalInfoBox = generateInfoBox();
			infoBoxManager.addInfoBox(ecumenicalInfoBox);
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

	private void updateCounts() {
		// Get ecumenical amounts from config file and update the class variables
		String inventoryCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_INV);
		String bankCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_KEY_BANK);
		totalKeyCount = parseIntSafely(inventoryCountStr) + parseIntSafely(bankCountStr);

		String inventoryShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_INV);
		String bankShardCountStr = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, CONFIG_SHARD_BANK);
		totalShardCount = parseIntSafely(inventoryShardCountStr) + parseIntSafely(bankShardCountStr);
	}

	public String generateInfoMessage() {
		StringBuilder message = new StringBuilder();

		// Construct tooltip message
		message.append(totalKeyCount).append("/").append(getMaxKeyAmount()).append(" keys");
		if (ecumenicalConfig.showShardCount() && totalShardCount != 0) {
			message.append("</br>").append(totalShardCount).append(" (").append(shardsToKeys(totalShardCount)).append(") shards");
		}

		return message.toString();
	}

	private InfoBox generateInfoBox() {
		return new InfoBox(itemManager.getImage(ItemID.ECUMENICAL_KEY), this) {
			@Override
			public String getText() {
				return String.valueOf(totalKeyCount);
			}

			@Override
			public Color getTextColor() {
				if (totalKeyCount >= getMaxKeyAmount()) {
					return Color.GREEN;
				} else if (totalKeyCount <= 0) {
					return Color.RED;
				}
				return Color.WHITE;
			}

			@Override
			public String getTooltip() {
				return generateInfoMessage();
			}
		};
	}

	private int parseIntSafely(String value) {
		try {
			return (value != null) ? Integer.parseInt(value) : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
