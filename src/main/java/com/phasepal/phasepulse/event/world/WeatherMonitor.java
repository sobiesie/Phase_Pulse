package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * Monitors weather changes (clear, rain, thunder).
 */
public class WeatherMonitor {
    private static final int SAMPLE_INTERVAL = 20; // Check every 20 ticks (1 second)

    private final EventDebouncer debouncer = new EventDebouncer();
    private WeatherState lastWeather = WeatherState.CLEAR;
    private int tickCounter = 0;

    private enum WeatherState {
        CLEAR,
        RAIN,
        THUNDER
    }

    public void onClientTick(Minecraft client) {
        if (client.level == null) {
            return;
        }

        // Only sample every SAMPLE_INTERVAL ticks - weather changes infrequently
        tickCounter++;
        if (tickCounter < SAMPLE_INTERVAL) {
            return;
        }
        tickCounter = 0;

        WeatherState currentWeather;
        if (client.level.isThundering()) {
            currentWeather = WeatherState.THUNDER;
        } else if (client.level.isRaining()) {
            currentWeather = WeatherState.RAIN;
        } else {
            currentWeather = WeatherState.CLEAR;
        }

        // Detect weather change
        if (currentWeather != lastWeather) {
            String eventName = switch (currentWeather) {
                case CLEAR -> "weather_clear";
                case RAIN -> "weather_rain";
                case THUNDER -> "weather_thunder";
            };

            if (debouncer.shouldTrigger("weather_change")) {
                EventPacket packet = new EventPacket(eventName);
                NetworkManager.getInstance().sendEvent(packet);
            }

            lastWeather = currentWeather;
        }
    }
}
