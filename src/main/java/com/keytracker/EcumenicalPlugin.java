package com.keytracker;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
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
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

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

	private int getEcumenicalKeyCount() {
		int varPlayerId = VarPlayer.BIRD_HOUSE_MEADOW_NORTH; // ECUMENICAL_KEY_COUNT.getId();

		return client.getVarbitValue(varPlayerId);
	}

	public String generateOverlayText() {
		StringBuilder message = new StringBuilder();
		// Current key count
		// TODO Get amount of keys stored
		message.append("T");

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
