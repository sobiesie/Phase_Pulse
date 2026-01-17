package com.phasepal.phasepulse.event;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import com.phasepal.phasepulse.event.combat.HostileMobDetector;
import com.phasepal.phasepulse.event.player.*;
import com.phasepal.phasepulse.event.world.BiomeTracker;
import com.phasepal.phasepulse.event.world.TimeMonitor;
import com.phasepal.phasepulse.event.world.WeatherMonitor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Centralized event registration hub.
 * Registers all event listeners based on configuration.
 */
public class EventRegistry {
    private static boolean registered = false;

    // Event monitors
    private static HealthMonitor healthMonitor;
    private static HungerMonitor hungerMonitor;
    private static DrowningMonitor drowningMonitor;
    private static SleepListener sleepListener;
    private static TimeMonitor timeMonitor;
    private static WeatherMonitor weatherMonitor;
    private static BiomeTracker biomeTracker;
    private static CombatTracker combatTracker;
    private static HostileMobDetector hostileMobDetector;

    /**
     * Registers all event listeners based on configuration.
     */
    public static void registerAll() {
        if (registered) {
            PhasePulse.LOGGER.warn("Events already registered");
            return;
        }

        PhasePulseConfig config = ConfigManager.getConfig();
        PhasePulse.LOGGER.info("Registering event listeners...");

        // Player state events
        if (config.sendPlayerEvents) {
            healthMonitor = new HealthMonitor();
            hungerMonitor = new HungerMonitor();
            drowningMonitor = new DrowningMonitor();
            sleepListener = new SleepListener();

            // Register client tick for player monitoring
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.player != null) {
                    healthMonitor.onClientTick(client);
                    hungerMonitor.onClientTick(client);
                    drowningMonitor.onClientTick(client);
                }
            });

            // Sleep listener registers itself via Fabric events
            sleepListener.register();

            PhasePulse.LOGGER.info("Registered player state events");
        }

        // World events
        if (config.sendWeatherEvents || config.sendBiomeEvents) {
            if (config.sendWeatherEvents) {
                timeMonitor = new TimeMonitor();
                weatherMonitor = new WeatherMonitor();
            }

            if (config.sendBiomeEvents) {
                biomeTracker = new BiomeTracker();
            }

            // Register world tick for world monitoring
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.world != null && client.player != null) {
                    if (config.sendWeatherEvents) {
                        timeMonitor.onClientTick(client);
                        weatherMonitor.onClientTick(client);
                    }
                    if (config.sendBiomeEvents) {
                        biomeTracker.onClientTick(client);
                    }
                }
            });

            PhasePulse.LOGGER.info("Registered world events");
        }

        // Combat events
        if (config.sendCombatEvents) {
            combatTracker = new CombatTracker();
            hostileMobDetector = new HostileMobDetector();

            // Register combat tracking
            combatTracker.register();

            // Register combat tracker and hostile mob detection via client tick
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.player != null && client.world != null) {
                    combatTracker.onClientTick();
                    hostileMobDetector.onClientTick(client);
                }
            });

            PhasePulse.LOGGER.info("Registered combat events");
        }

        registered = true;
        PhasePulse.LOGGER.info("Event registration complete");
    }

    /**
     * Gets the combat tracker instance for damage event reporting.
     * @return The combat tracker, or null if combat events are disabled
     */
    public static CombatTracker getCombatTracker() {
        return combatTracker;
    }

    /**
     * Unregisters all event listeners and cleans up.
     */
    public static void unregisterAll() {
        // Cleanup monitors
        healthMonitor = null;
        hungerMonitor = null;
        drowningMonitor = null;
        sleepListener = null;
        timeMonitor = null;
        weatherMonitor = null;
        biomeTracker = null;
        combatTracker = null;
        hostileMobDetector = null;

        registered = false;
        PhasePulse.LOGGER.info("Event listeners unregistered");
    }
}
