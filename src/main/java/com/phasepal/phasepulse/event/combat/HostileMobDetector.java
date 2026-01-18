package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Detects nearby hostile mobs within a 16-block radius.
 * Scans every 40 ticks (2 seconds) for performance.
 */
public class HostileMobDetector {
    private static final double DETECTION_RADIUS = 16.0;
    private static final int SCAN_INTERVAL = 40; // Ticks (2 seconds)
    private static final long DEBOUNCE_MS = 10000; // 10 seconds between notifications

    private final EventDebouncer debouncer = new EventDebouncer();
    private int tickCounter = 0;

    public void onClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        // Only scan every SCAN_INTERVAL ticks
        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Create bounding box around player
        Box searchBox = client.player.getBoundingBox().expand(DETECTION_RADIUS);

        // Find hostile entities in range - single pass to count and get first type
        List<Entity> nearbyEntities = client.world.getOtherEntities(client.player, searchBox);
        int hostileCount = 0;
        String firstMobType = null;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof HostileEntity) {
                hostileCount++;
                if (firstMobType == null) {
                    firstMobType = entity.getType().getTranslationKey();
                }
            }
        }

        if (hostileCount > 0 && debouncer.shouldTrigger("hostile_mob_nearby", DEBOUNCE_MS)) {
            EventPacket packet = new EventPacket("hostile_mob_nearby")
                    .addMetadata("mob_type", firstMobType)
                    .addMetadata("count", hostileCount);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
