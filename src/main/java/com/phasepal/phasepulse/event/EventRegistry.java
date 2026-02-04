package com.phasepal.phasepulse.event;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import com.phasepal.phasepulse.event.combat.HostileMobDetector;
import com.phasepal.phasepulse.event.combat.MobKilledListener;
import com.phasepal.phasepulse.event.player.*;
import com.phasepal.phasepulse.event.world.BiomeTracker;
import com.phasepal.phasepulse.event.world.StructureTracker;
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
    private static HurtListener hurtListener;
    private static SleepListener sleepListener;
    private static DeathListener deathListener;
    private static StatusEffectListener statusEffectListener;
    private static InventoryListener inventoryListener;
    private static RareItemListener rareItemListener;
    // private static BlockPlacedListener blockPlacedListener;
    private static BlockBrokenListener blockBrokenListener;
    private static TimeMonitor timeMonitor;
    private static WeatherMonitor weatherMonitor;
    private static BiomeTracker biomeTracker;
    private static StructureTracker structureTracker;
    private static CombatTracker combatTracker;
    private static HostileMobDetector hostileMobDetector;
    private static MobKilledListener mobKilledListener;

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
            hurtListener = new HurtListener();
            sleepListener = new SleepListener();
            deathListener = new DeathListener();
            statusEffectListener = new StatusEffectListener();
            inventoryListener = new InventoryListener();
            rareItemListener = new RareItemListener();
            // blockPlacedListener = new BlockPlacedListener();
            blockBrokenListener = new BlockBrokenListener();

            // Register Fabric event listeners
            sleepListener.register();
            // blockPlacedListener.register();
            // blockBrokenListener uses mixin (BlockBreakMixin)

            PhasePulse.LOGGER.info("Registered player state events");
        }

        if (config.sendWeatherEvents) {
            timeMonitor = new TimeMonitor();
            weatherMonitor = new WeatherMonitor();
        }

        if (config.sendBiomeEvents) {
            biomeTracker = new BiomeTracker();
            structureTracker = new StructureTracker();
        }

        if (config.sendWeatherEvents || config.sendBiomeEvents) {
            PhasePulse.LOGGER.info("Registered world events");
        }

        if (config.sendCombatEvents) {
            combatTracker = new CombatTracker();
            hostileMobDetector = new HostileMobDetector();
            mobKilledListener = new MobKilledListener();

            // Register combat tracking (combatTracker uses mixin, mobKilledListener uses client tick)
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
            if (hurtListener != null) {
                hurtListener.onClientTick(client);
            }
            if (deathListener != null) {
                deathListener.onClientTick(client);
            }
            if (statusEffectListener != null) {
                statusEffectListener.onClientTick(client);
            }
            if (inventoryListener != null) {
                inventoryListener.onClientTick(client);
            }
            if (rareItemListener != null) {
                rareItemListener.onClientTick(client);
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
                if (structureTracker != null) {
                    structureTracker.onClientTick(client);
                }

                // Combat monitors
                if (combatTracker != null) {
                    combatTracker.onClientTick();
                }
                if (hostileMobDetector != null) {
                    hostileMobDetector.onClientTick(client);
                }
                if (mobKilledListener != null) {
                    mobKilledListener.onClientTick(client);
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
        hurtListener = null;
        sleepListener = null;
        deathListener = null;
        statusEffectListener = null;
        inventoryListener = null;
        rareItemListener = null;
        // blockPlacedListener = null;
        blockBrokenListener = null;
        timeMonitor = null;
        weatherMonitor = null;
        biomeTracker = null;
        structureTracker = null;
        combatTracker = null;
        hostileMobDetector = null;
        mobKilledListener = null;

        registered = false;
        PhasePulse.LOGGER.info("Event listeners unregistered");
    }
}
