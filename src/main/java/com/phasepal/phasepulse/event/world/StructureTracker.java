package com.phasepal.phasepulse.event.world;

import net.minecraft.client.Minecraft;

/**
 * Temporary placeholder while structure APIs are remapped for NeoForge/Mojmap.
 * Client-side structure detection is disabled in this porting step.
 */
public class StructureTracker {
    private static final int SAMPLE_INTERVAL = 60;
    private int tickCounter = 0;

    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }

        tickCounter++;
        if (tickCounter < SAMPLE_INTERVAL) {
            return;
        }
        tickCounter = 0;
    }
}
