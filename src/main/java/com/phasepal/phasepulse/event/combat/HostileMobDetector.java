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
        Box searchBox = new Box(
                client.player.getX() - DETECTION_RADIUS,
                client.player.getY() - DETECTION_RADIUS,
                client.player.getZ() - DETECTION_RADIUS,
                client.player.getX() + DETECTION_RADIUS,
                client.player.getY() + DETECTION_RADIUS,
                client.player.getZ() + DETECTION_RADIUS
        );

        // Find hostile entities in range
        List<Entity> nearbyEntities = client.world.getOtherEntities(client.player, searchBox);
        long hostileCount = nearbyEntities.stream()
                .filter(entity -> entity instanceof HostileEntity)
                .count();

        if (hostileCount > 0) {
            // Find the closest hostile mob type
            String mobType = nearbyEntities.stream()
                    .filter(entity -> entity instanceof HostileEntity)
                    .map(entity -> entity.getType().toString())
                    .findFirst()
                    .orElse("unknown");

            if (debouncer.shouldTrigger("hostile_mob_nearby", DEBOUNCE_MS)) {
                EventPacket packet = new EventPacket("hostile_mob_nearby")
                        .addMetadata("mob_type", mobType)
                        .addMetadata("count", hostileCount);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
