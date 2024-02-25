package com.keytracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class EcumenicalOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private EcumenicalPlugin plugin;

    @Override
    public Dimension render(Graphics2D graphics) {
        // Get idemId from current menu
        final MenuEntry[] menuEntries = client.getMenuEntries();
        final int menuSize = menuEntries.length;
        if (menuSize == 0) {
            return null;
        }

        final MenuEntry menuEntry = menuEntries[menuSize - 1];
        final Widget widget = menuEntry.getWidget();
        if (widget == null) {
            return null;
        }

        int itemId = widget.getItemId();
        if (itemId == -1) {
            return null;
        }

        // Only render when hovering ecumenical items
        if (itemId != ItemID.ECUMENICAL_KEY && itemId != ItemID.ECUMENICAL_KEY_SHARD) {
            return null;
        }

        // Render tooltip
        String overlayText = plugin.generateInfoMessage();
        if (overlayText != null) {
            tooltipManager.add(new Tooltip(plugin.generateInfoMessage()));
        }
        return null;
    }
}
