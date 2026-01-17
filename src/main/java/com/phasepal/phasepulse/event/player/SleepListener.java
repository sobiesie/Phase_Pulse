package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Listens for player sleep and wake events using Fabric API.
 */
public class SleepListener {
    private final EventDebouncer debouncer = new EventDebouncer();

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
}
