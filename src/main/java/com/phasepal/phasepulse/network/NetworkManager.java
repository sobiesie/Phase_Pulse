package com.phasepal.phasepulse.network;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.ConfigManager;
import com.phasepal.phasepulse.config.PhasePulseConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Singleton network manager for Phase_Pulse.
 * Manages the network thread lifecycle and provides the main interface for sending events.
 */
public class NetworkManager {
    private static NetworkManager instance;

    private final PhasePulseConfig config;
    private final ConnectionHandler connectionHandler;
    private final PacketSender packetSender;
    private Thread senderThread;

    private NetworkManager() {
        this.config = ConfigManager.getConfig();
        this.connectionHandler = new ConnectionHandler(config);
        this.packetSender = new PacketSender(config, connectionHandler);
    }

    /**
     * Gets the singleton instance.
     * @return The NetworkManager instance
     */
    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    /**
     * Initializes and starts the network thread.
     */
    public synchronized void initialize() {
        if (senderThread != null && senderThread.isAlive()) {
            PhasePulse.LOGGER.warn("Network manager already initialized");
            return;
        }

        if (!config.enabled) {
            PhasePulse.LOGGER.info("Phase_Pulse is disabled in config");
            return;
        }

        senderThread = new Thread(packetSender, "PhasePulse-Network");
        senderThread.setDaemon(false); // Non-daemon for clean shutdown
        senderThread.start();

        PhasePulse.LOGGER.info("Network manager initialized - ready to connect to Phase Pal");
    }

    /**
     * Shuts down the network thread and disconnects.
     */
    public synchronized void shutdown() {
        if (packetSender != null) {
            packetSender.stop();
        }

        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
            try {
                senderThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                PhasePulse.LOGGER.warn("Interrupted while waiting for network thread to stop");
            }
        }

        PhasePulse.LOGGER.info("Network manager shut down");
    }

    /**
     * Sends an event to Phase Pal.
     * This is the main public interface for sending events.
     * @param eventName The event name
     */
    public void sendEvent(String eventName) {
        sendEvent(new EventPacket(eventName));
    }

    /**
     * Sends an event with metadata to Phase Pal.
     * Automatically injects world metadata into every packet.
     * @param packet The event packet to send
     */
    public void sendEvent(EventPacket packet) {
        if (!config.enabled || packetSender == null) {
            return;
        }

        // Inject world metadata
        injectWorldMetadata(packet);

        packetSender.queuePacket(packet);
    }

    /**
     * Injects world metadata into the packet.
     * Adds world_name, world_type, and dimension to every event.
     */
    private void injectWorldMetadata(EventPacket packet) {
        Minecraft client = Minecraft.getInstance();

        if (client.level == null) {
            return;
        }

        // Determine world name and type
        String worldName;
        String worldType;

        ServerData serverInfo = client.getCurrentServer();
        if (serverInfo != null) {
            // Multiplayer server
            worldName = serverInfo.name;
            worldType = "multiplayer";
        } else if (client.hasSingleplayerServer()) {
            // Singleplayer world
            worldName = "singleplayer";
            worldType = "singleplayer";
        } else {
            worldName = "unknown";
            worldType = "unknown";
        }

        // Get dimension (overworld, the_nether, the_end)
        String dimension = client.level.dimension().identifier().getPath();

        packet.addMetadata("world_name", worldName);
        packet.addMetadata("world_type", worldType);
        packet.addMetadata("dimension", dimension);
    }

    /**
     * Checks if the network manager is connected to Phase Pal.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connectionHandler != null && connectionHandler.isConnected();
    }

    /**
     * Gets the current packet queue size.
     * @return Number of packets waiting to be sent
     */
    public int getQueueSize() {
        return packetSender != null ? packetSender.getQueueSize() : 0;
    }

    /**
     * Gets the configuration.
     * @return The configuration instance
     */
    public PhasePulseConfig getConfig() {
        return config;
    }
}
