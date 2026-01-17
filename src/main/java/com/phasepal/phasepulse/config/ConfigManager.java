package com.phasepal.phasepulse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phasepal.phasepulse.PhasePulse;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading and saving of the Phase_Pulse configuration file.
 * Config location: config/phasepulse.json
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("phasepulse.json");

    private static PhasePulseConfig config = null;

    /**
     * Loads the configuration from disk, or creates default config if not found.
     * @return The loaded or default configuration
     */
    public static PhasePulseConfig loadConfig() {
        if (config != null) {
            return config;
        }

        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, PhasePulseConfig.class);

                // Validate loaded config
                if (config == null || !config.validate()) {
                    PhasePulse.LOGGER.warn("Invalid configuration detected, using defaults");
                    config = new PhasePulseConfig();
                    saveConfig();
                } else {
                    PhasePulse.LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
                }
            } else {
                PhasePulse.LOGGER.info("No configuration file found, creating default at {}", CONFIG_PATH);
                config = new PhasePulseConfig();
                saveConfig();
            }
        } catch (IOException e) {
            PhasePulse.LOGGER.error("Failed to load configuration, using defaults", e);
            config = new PhasePulseConfig();
        } catch (Exception e) {
            PhasePulse.LOGGER.error("Failed to parse configuration, using defaults", e);
            config = new PhasePulseConfig();
            try {
                saveConfig();
            } catch (Exception saveError) {
                PhasePulse.LOGGER.error("Failed to save default configuration", saveError);
            }
        }

        return config;
    }

    /**
     * Saves the current configuration to disk.
     */
    public static void saveConfig() {
        if (config == null) {
            config = new PhasePulseConfig();
        }

        try {
            // Ensure config directory exists
            Files.createDirectories(CONFIG_PATH.getParent());

            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);

            PhasePulse.LOGGER.info("Configuration saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            PhasePulse.LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Gets the current configuration instance.
     * @return The current configuration, or loads it if not yet loaded
     */
    public static PhasePulseConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
}
