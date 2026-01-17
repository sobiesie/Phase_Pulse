package com.phasepal.phasepulse.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a JSON event packet sent to Phase Pal.
 * Format: {"event":"event_name","timestamp":1234567890,"metadata":{}}
 */
public class EventPacket {
    private static final Gson GSON = new GsonBuilder().create();

    private final String event;
    private final long timestamp;
    private final Map<String, Object> metadata;

    public EventPacket(String event) {
        this(event, new HashMap<>());
    }

    public EventPacket(String event, Map<String, Object> metadata) {
        this.event = event;
        this.timestamp = System.currentTimeMillis() / 1000; // Unix timestamp in seconds
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    /**
     * Adds metadata to the packet.
     * @param key Metadata key
     * @param value Metadata value
     * @return This packet for chaining
     */
    public EventPacket addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Serializes the packet to JSON string.
     * @return JSON representation of the packet
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * Gets the size of the packet in bytes (UTF-8).
     * @return Size in bytes
     */
    public int getSize() {
        return toJson().getBytes().length;
    }

    public String getEvent() {
        return event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return toJson();
    }
}
