package com.phasepal.phasepulse.network;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.PhasePulseConfig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the TCP socket connection lifecycle to Phase Pal.
 * Handles connection, disconnection, and sending data.
 */
public class ConnectionHandler {
    private final PhasePulseConfig config;
    private Socket socket;
    private PrintWriter writer;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private long lastConnectionAttempt = 0;
    private static final long RECONNECT_DELAY_MS = 5000; // 5 seconds between reconnect attempts

    public ConnectionHandler(PhasePulseConfig config) {
        this.config = config;
    }

    /**
     * Attempts to connect to Phase Pal.
     * @return true if connection successful, false otherwise
     */
    public synchronized boolean connect() {
        if (connected.get()) {
            return true;
        }

        // Rate limit connection attempts
        long now = System.currentTimeMillis();
        if (now - lastConnectionAttempt < RECONNECT_DELAY_MS) {
            return false;
        }
        lastConnectionAttempt = now;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(config.host, config.port), config.connectionTimeoutMs);
            socket.setTcpNoDelay(true); // Disable Nagle's algorithm for low latency
            socket.setSoTimeout(1000); // 1 second read timeout

            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                    false // Don't auto-flush on println
            );

            connected.set(true);
            PhasePulse.LOGGER.info("Connected to Phase Pal at {}:{}", config.host, config.port);
            return true;

        } catch (IOException e) {
            if (config.debugLogging) {
                PhasePulse.LOGGER.debug("Failed to connect to Phase Pal: {}", e.getMessage());
            }
            disconnect();
            return false;
        }
    }

    /**
     * Disconnects from Phase Pal.
     */
    public synchronized void disconnect() {
        connected.set(false);

        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                // Ignore
            }
            writer = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
            socket = null;
        }

        if (config.debugLogging) {
            PhasePulse.LOGGER.debug("Disconnected from Phase Pal");
        }
    }

    /**
     * Sends a packet to Phase Pal.
     * @param packet The packet to send
     * @return true if sent successfully, false otherwise
     */
    public synchronized boolean sendPacket(EventPacket packet) {
        if (!connected.get() || writer == null) {
            if (config.reconnectOnFailure) {
                connect();
            }
            if (!connected.get()) {
                return false;
            }
        }

        try {
            String json = packet.toJson();
            writer.println(json);
            writer.flush();

            if (writer.checkError()) {
                PhasePulse.LOGGER.warn("Error writing to Phase Pal socket");
                disconnect();
                return false;
            }

            if (config.debugLogging) {
                PhasePulse.LOGGER.debug("Sent packet: {}", json);
            }

            return true;

        } catch (Exception e) {
            PhasePulse.LOGGER.warn("Failed to send packet: {}", e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Checks if currently connected to Phase Pal.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected.get() && socket != null && socket.isConnected() && !socket.isClosed();
    }
}
