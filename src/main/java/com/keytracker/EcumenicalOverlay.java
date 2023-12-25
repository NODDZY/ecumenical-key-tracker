package com.keytracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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
        if(widget == null) {
            return null;
        }

        int itemId = widget.getItemId();
        if(itemId == -1) {
            return null;
        }

        // Only render when hovering ecumenical key item
        if(itemId != ItemID.ECUMENICAL_KEY) {
            return null;
        }

        // Render tooltip
        tooltipManager.add(new Tooltip(plugin.generateOverlayText()));
        return null;
    }
}
