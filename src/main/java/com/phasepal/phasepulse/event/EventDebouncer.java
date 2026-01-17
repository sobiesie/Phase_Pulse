package com.phasepal.phasepulse.event;

import com.phasepal.phasepulse.config.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic event debouncing utility to prevent event spam.
 * Thread-safe for concurrent access from multiple event sources.
 */
public class EventDebouncer {
    private final ConcurrentHashMap<String, Long> lastTriggerTimes = new ConcurrentHashMap<>();

    /**
     * Checks if an event should be triggered based on debounce timing.
     * @param eventKey Unique key for the event
     * @return true if event should trigger, false if still in debounce period
     */
    public boolean shouldTrigger(String eventKey) {
        return shouldTrigger(eventKey, ConfigManager.getConfig().debounceMs);
    }

    /**
     * Checks if an event should be triggered with custom debounce delay.
     * @param eventKey Unique key for the event
     * @param debounceMs Debounce delay in milliseconds
     * @return true if event should trigger, false if still in debounce period
     */
    public boolean shouldTrigger(String eventKey, long debounceMs) {
        long now = System.currentTimeMillis();
        Long lastTime = lastTriggerTimes.get(eventKey);

        if (lastTime == null || (now - lastTime) >= debounceMs) {
            lastTriggerTimes.put(eventKey, now);
            return true;
        }

        return false;
    }

    /**
     * Forces a trigger time update without checking.
     * Useful for resetting debounce state.
     * @param eventKey Unique key for the event
     */
    public void updateTriggerTime(String eventKey) {
        lastTriggerTimes.put(eventKey, System.currentTimeMillis());
    }

    /**
     * Clears the debounce state for a specific event.
     * @param eventKey Unique key for the event
     */
    public void clearEvent(String eventKey) {
        lastTriggerTimes.remove(eventKey);
    }

    /**
     * Clears all debounce state.
     */
    public void clearAll() {
        lastTriggerTimes.clear();
    }

    /**
     * Gets the time since last trigger for an event.
     * @param eventKey Unique key for the event
     * @return Milliseconds since last trigger, or Long.MAX_VALUE if never triggered
     */
    public long getTimeSinceLastTrigger(String eventKey) {
        Long lastTime = lastTriggerTimes.get(eventKey);
        if (lastTime == null) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - lastTime;
    }
}
