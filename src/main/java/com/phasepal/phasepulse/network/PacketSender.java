package com.phasepal.phasepulse.network;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.config.PhasePulseConfig;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async packet sender with rate limiting and queue management.
 * Runs on a dedicated network thread to avoid blocking the game thread.
 */
public class PacketSender implements Runnable {
    private static final int MAX_QUEUE_SIZE = 100;

    private final PhasePulseConfig config;
    private final ConnectionHandler connectionHandler;
    private final BlockingQueue<EventPacket> packetQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastSendTime = 0;

    public PacketSender(PhasePulseConfig config, ConnectionHandler connectionHandler) {
        this.config = config;
        this.connectionHandler = connectionHandler;
        this.packetQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    }

    /**
     * Queues a packet for sending.
     * If queue is full, drops the oldest packet.
     * @param packet The packet to queue
     */
    public void queuePacket(EventPacket packet) {
        if (!config.enabled) {
            return;
        }

        // Check packet size limit (2KB max)
        if (packet.getSize() > 2048) {
            PhasePulse.LOGGER.warn("Packet too large ({}B), dropping: {}", packet.getSize(), packet.getEvent());
            return;
        }

        // If queue is full, remove oldest packet
        if (!packetQueue.offer(packet)) {
            EventPacket dropped = packetQueue.poll();
            if (dropped != null && config.debugLogging) {
                PhasePulse.LOGGER.debug("Queue full, dropped packet: {}", dropped.getEvent());
            }
            packetQueue.offer(packet);
        }
    }

    /**
     * Main thread loop - processes packet queue with rate limiting.
     * Periodically attempts reconnection if disconnected.
     */
    @Override
    public void run() {
        running.set(true);
        PhasePulse.LOGGER.info("Packet sender thread started");

        // Initial connection attempt
        connectionHandler.connect();

        while (running.get()) {
            try {
                // Try to get a packet with timeout (5 seconds)
                // This allows periodic reconnection attempts even when idle
                EventPacket packet = packetQueue.poll(5, java.util.concurrent.TimeUnit.SECONDS);

                // If no packet available, try to reconnect if disconnected
                if (packet == null) {
                    if (config.reconnectOnFailure && !connectionHandler.isConnected()) {
                        connectionHandler.connect();
                    }
                    continue;
                }

                // Ensure we're connected before sending
                if (!connectionHandler.isConnected() && config.reconnectOnFailure) {
                    connectionHandler.connect();
                }

                // Rate limiting: enforce minimum time between packets
                long now = System.currentTimeMillis();
                long timeSinceLastSend = now - lastSendTime;
                long minDelay = config.debounceMs;

                if (timeSinceLastSend < minDelay) {
                    Thread.sleep(minDelay - timeSinceLastSend);
                }

                // Send the packet
                boolean sent = connectionHandler.sendPacket(packet);
                if (sent) {
                    lastSendTime = System.currentTimeMillis();
                } else {
                    // Failed to send, put back in queue if there's room
                    if (config.reconnectOnFailure) {
                        packetQueue.offer(packet);
                        // Back off for a bit before retry
                        Thread.sleep(1000);
                    }
                }

            } catch (InterruptedException e) {
                // Thread interrupted, exit loop
                break;
            } catch (Exception e) {
                PhasePulse.LOGGER.error("Error in packet sender thread", e);
            }
        }

        // Cleanup
        connectionHandler.disconnect();
        packetQueue.clear();
        PhasePulse.LOGGER.info("Packet sender thread stopped");
    }

    /**
     * Stops the packet sender thread.
     */
    public void stop() {
        running.set(false);
    }

    /**
     * Checks if the sender thread is running.
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Gets the current queue size.
     * @return Number of packets in queue
     */
    public int getQueueSize() {
        return packetQueue.size();
    }
}
