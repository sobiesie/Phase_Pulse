package com.phasepal.phasepulse;

import com.phasepal.phasepulse.command.PalCommand;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.network.NetworkManager;

/**
 * Client-side initialization for Phase_Pulse mod.
 * Sets up configuration, networking, and event listeners.
 */
public final class PhasePulseClient {
	private PhasePulseClient() {
	}

	public static void initialize() {
		PhasePulse.LOGGER.info("Initializing Phase_Pulse client...");

		// Load configuration
		PhasePulseConfig config = ConfigManager.loadConfig();

		// Initialize network layer if enabled
		if (config.enabled) {
			NetworkManager.getInstance().initialize();
			PhasePulse.LOGGER.info("Phase_Pulse network initialized - companion link ready");

			// Register event listeners
			EventRegistry.registerAll();

			// NeoForge client command hookup is pending; keep stub call for parity.
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
