package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors weather changes (clear, rain, thunder).
 */
public class WeatherMonitor {
    private final EventDebouncer debouncer = new EventDebouncer();
    private WeatherState lastWeather = WeatherState.CLEAR;

    private enum WeatherState {
        CLEAR,
        RAIN,
        THUNDER
    }

    public void onClientTick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        WeatherState currentWeather;
        if (client.world.isThundering()) {
            currentWeather = WeatherState.THUNDER;
        } else if (client.world.isRaining()) {
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
