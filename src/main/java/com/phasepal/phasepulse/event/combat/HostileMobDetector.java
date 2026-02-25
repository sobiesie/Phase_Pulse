package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Detects nearby hostile mobs within a 16-block radius.
 * Scans every 40 ticks (2 seconds) for performance.
 */
public class HostileMobDetector {
    private static final double DETECTION_RADIUS = 16.0;
    private static final double DETECTION_RADIUS_VERTICAL = 4.0; // Reduced to avoid detecting mobs in caves
    private static final double COMBAT_RADIUS = 8.0; // Closer range for combat detection
    private static final int SCAN_INTERVAL = 40; // Ticks (2 seconds)
    private static final long DEBOUNCE_MS = 10000; // 10 seconds between notifications

    private final EventDebouncer debouncer = new EventDebouncer();
    private int tickCounter = 0;

    /**
     * Checks if there are hostile mobs within combat range of the player.
     * Used to determine if damage should trigger combat state.
     * @return true if hostile mobs are within COMBAT_RADIUS blocks
     */
    public static boolean areHostilesNearby() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return false;
        }

        AABB searchBox = new AABB(
                client.player.getX() - COMBAT_RADIUS,
                client.player.getY() - COMBAT_RADIUS,
                client.player.getZ() - COMBAT_RADIUS,
                client.player.getX() + COMBAT_RADIUS,
                client.player.getY() + COMBAT_RADIUS,
                client.player.getZ() + COMBAT_RADIUS
        );

        return client.level.getEntities(client.player, searchBox, entity -> true).stream()
                .anyMatch(entity -> entity instanceof Enemy);
    }

    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }

        // Only scan every SCAN_INTERVAL ticks
        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Create bounding box around player (limited vertical range to avoid cave mobs)
        AABB searchBox = client.player.getBoundingBox().inflate(DETECTION_RADIUS, DETECTION_RADIUS_VERTICAL, DETECTION_RADIUS);

        // Find hostile entities in range - single pass to count and get first type
        List<Entity> nearbyEntities = client.level.getEntities(client.player, searchBox, entity -> true);
        int hostileCount = 0;
        String firstMobType = null;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Enemy) {
                hostileCount++;
                if (firstMobType == null) {
                    firstMobType = entity.getType().getDescriptionId();
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
