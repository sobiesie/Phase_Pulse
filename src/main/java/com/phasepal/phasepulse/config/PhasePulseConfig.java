package com.phasepal.phasepulse.config;

/**
 * Configuration data structure for Phase_Pulse mod.
 * Loaded from config/phasepulse.json with sensible defaults.
 */
public class PhasePulseConfig {
    public boolean enabled = true;
    public String host = "localhost";
    public int port = 32145;
    public int debounceMs = 250;
    public boolean sendPlayerEvents = true;
    public boolean sendCombatEvents = true;
    public boolean sendBiomeEvents = true;
    public boolean sendWeatherEvents = true;
    public int maxPacketsPerSecond = 5;
    public int connectionTimeoutMs = 5000;
    public boolean reconnectOnFailure = true;
    public boolean debugLogging = false;

    /**
     * Validates the configuration values.
     * @return true if configuration is valid, false otherwise
     */
    public boolean validate() {
        // Ensure host is localhost or 127.0.0.1 for security
        if (!host.equals("localhost") && !host.equals("127.0.0.1")) {
            return false;
        }

        // Validate port range
        if (port < 1024 || port > 65535) {
            return false;
        }

        // Validate debounce timing
        if (debounceMs < 50 || debounceMs > 5000) {
            return false;
        }

        // Validate rate limiting
        if (maxPacketsPerSecond < 1 || maxPacketsPerSecond > 100) {
            return false;
        }

        // Validate connection timeout
        if (connectionTimeoutMs < 1000 || connectionTimeoutMs > 30000) {
            return false;
        }

        return true;
    }
}
