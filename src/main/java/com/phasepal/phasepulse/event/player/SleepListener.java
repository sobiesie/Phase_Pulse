package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Listens for player sleep and wake events using Fabric API.
 * Also handles sleep failure events via SleepMixin.
 */
public class SleepListener {
    private final EventDebouncer debouncer = new EventDebouncer();

    // Static debouncer for mixin callbacks
    private static final EventDebouncer staticDebouncer = new EventDebouncer();

    public void register() {
        // Player starts sleeping
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof PlayerEntity && entity.getEntityWorld().isClient()) {
                if (debouncer.shouldTrigger("player_sleep")) {
                    long worldTime = entity.getEntityWorld().getTimeOfDay();
                    EventPacket packet = new EventPacket("player_sleep")
                            .addMetadata("world_time", worldTime);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
        });

        // Player stops sleeping (wakes up)
        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof PlayerEntity && entity.getEntityWorld().isClient()) {
                if (debouncer.shouldTrigger("player_wake")) {
                    long worldTime = entity.getEntityWorld().getTimeOfDay();
                    EventPacket packet = new EventPacket("player_wake")
                            .addMetadata("world_time", worldTime);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
        });
    }

    /**
     * Called from SleepMixin when a sleep attempt fails.
     * @param reason The reason sleep failed
     */
    public static void onSleepFailed(PlayerEntity.SleepFailureReason reason) {
        // Debounce to prevent spam (3 second cooldown for sleep failures)
        if (!staticDebouncer.shouldTrigger("sleep_failed", 3000)) {
            return;
        }

        // Use toString() to get the reason name
        String reasonString = reason.toString().toLowerCase();
        boolean isMonstersNearby = reasonString.contains("safe");

        // Convert to readable string
        String readableReason = reasonString.contains("safe") ? "monsters_nearby" : reasonString;

        EventPacket packet = new EventPacket("sleep_failed")
                .addMetadata("reason", readableReason)
                .addMetadata("monsters_nearby", isMonstersNearby);

        NetworkManager.getInstance().sendEvent(packet);
    }
}
