package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors world time for day/night transitions.
 * Day: 0-12000 ticks, Night: 12000-24000 ticks (repeating cycle)
 */
public class TimeMonitor {
    private static final long DAY_START = 0;
    private static final long DAY_END = 1000;
    private static final long NIGHT_START = 12000;
    private static final long NIGHT_END = 13000;

    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean isNight = false;

    public void onClientTick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        long timeOfDay = client.world.getTimeOfDay() % 24000;
        boolean shouldBeNight = timeOfDay >= 12000;

        // Detect day start transition (night -> day)
        if (isNight && !shouldBeNight && timeOfDay >= DAY_START && timeOfDay <= DAY_END) {
            if (debouncer.shouldTrigger("day_start")) {
                EventPacket packet = new EventPacket("day_start")
                        .addMetadata("world_time", client.world.getTimeOfDay());

                NetworkManager.getInstance().sendEvent(packet);
            }
        }

        // Detect night start transition (day -> night)
        if (!isNight && shouldBeNight && timeOfDay >= NIGHT_START && timeOfDay <= NIGHT_END) {
            if (debouncer.shouldTrigger("night_start")) {
                EventPacket packet = new EventPacket("night_start")
                        .addMetadata("world_time", client.world.getTimeOfDay());

                NetworkManager.getInstance().sendEvent(packet);
            }
        }

        isNight = shouldBeNight;
    }
}
