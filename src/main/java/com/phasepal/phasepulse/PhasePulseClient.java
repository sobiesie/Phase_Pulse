package com.phasepal.phasepulse;

import com.phasepal.phasepulse.command.PalCommand;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initialization for Phase_Pulse mod.
 * Sets up configuration, networking, and event listeners.
 */
public class PhasePulseClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PhasePulse.LOGGER.info("Initializing Phase_Pulse client...");

        // Load configuration
        PhasePulseConfig config = ConfigManager.loadConfig();

        // Initialize network layer if enabled
        if (config.enabled) {
            NetworkManager.getInstance().initialize();
            PhasePulse.LOGGER.info("Phase_Pulse network initialized - companion link ready");

            // Register event listeners
            EventRegistry.registerAll();

            // Register /pal chat command
            PalCommand.register();
        } else {
            PhasePulse.LOGGER.info("Phase_Pulse is disabled in configuration");
        }

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PhasePulse.LOGGER.info("Shutting down Phase_Pulse...");
            NetworkManager.getInstance().shutdown();
            EventRegistry.unregisterAll();
        }));

        PhasePulse.LOGGER.info("Phase_Pulse client initialization complete");
    }
}
