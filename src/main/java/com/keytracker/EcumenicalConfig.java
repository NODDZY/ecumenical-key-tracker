package com.keytracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(EcumenicalConfig.CONFIG_GROUP)
public interface EcumenicalConfig extends Config {
    String CONFIG_GROUP = "ecumenicalkeytracker";

    @ConfigItem(
            keyName = "showShardCount",
            name = "Show ecumenical shard count",
            description = "Display the ecumenical shard count on the tooltip",
            position = 1
    )
    default boolean showShardCount() {
        return true;
    }

    @ConfigSection(
            name = "Infobox",
            description = "Settings related to the infobox",
            position = 100
    )
    String SECTION_INFOBOX = "infoboxSection";

    @ConfigItem(
            keyName = "displayInfobox",
            name = "Display infobox",
            description = "Toggle to enable or disable the infobox",
            section = SECTION_INFOBOX,
            position = 101
    )
    default boolean displayInfobox() {
        return true;
    }

    @ConfigItem(
            keyName = "onlyInWildernessDungeon",
            name = "Only show infobox in WGWD",
            description = "Only display the infobox while inside the Wilderness God Wars Dungeon",
            section = SECTION_INFOBOX,
            position = 102
    )
    default boolean onlyDisplayWhileInDungeon() {
        return true;
    }
}
