package com.keytracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EcumenicalPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EcumenicalPlugin.class);
        RuneLite.main(args);
    }
}
