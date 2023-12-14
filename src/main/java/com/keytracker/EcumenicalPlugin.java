package com.keytracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Ecumenical Key Tracker"
)
public class EcumenicalPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private EcumenicalConfig config;

	@Provides
	EcumenicalConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(EcumenicalConfig.class);
	}
}
