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

        // Initialize monitors based on configuration
        if (config.sendPlayerEvents) {
            healthMonitor = new HealthMonitor();
            hungerMonitor = new HungerMonitor();
            drowningMonitor = new DrowningMonitor();
            sleepListener = new SleepListener();
            sleepListener.register();
            PhasePulse.LOGGER.info("Registered player state events");
        }

        if (config.sendWeatherEvents) {
            timeMonitor = new TimeMonitor();
            weatherMonitor = new WeatherMonitor();
        }

        if (config.sendBiomeEvents) {
            biomeTracker = new BiomeTracker();
        }

        if (config.sendWeatherEvents || config.sendBiomeEvents) {
            PhasePulse.LOGGER.info("Registered world events");
        }

        if (config.sendCombatEvents) {
            combatTracker = new CombatTracker();
            hostileMobDetector = new HostileMobDetector();
            combatTracker.register();
            PhasePulse.LOGGER.info("Registered combat events");
        }

        // Single consolidated tick handler for all monitors (reduces per-tick overhead)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            // Player monitors
            if (healthMonitor != null) {
                healthMonitor.onClientTick(client);
            }
            if (hungerMonitor != null) {
                hungerMonitor.onClientTick(client);
            }
            if (drowningMonitor != null) {
                drowningMonitor.onClientTick(client);
            }

            // World monitors (require world)
            if (client.world != null) {
                if (timeMonitor != null) {
                    timeMonitor.onClientTick(client);
                }
                if (weatherMonitor != null) {
                    weatherMonitor.onClientTick(client);
                }
                if (biomeTracker != null) {
                    biomeTracker.onClientTick(client);
                }

                // Combat monitors
                if (combatTracker != null) {
                    combatTracker.onClientTick();
                }
                if (hostileMobDetector != null) {
                    hostileMobDetector.onClientTick(client);
                }
            }
        });

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
