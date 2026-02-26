package com.phasepal.phasepulse.event.milestone;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * Tracks when player survives their first night without sleeping.
 * A milestone event for progression tracking.
 */
public class FirstNightTracker {
    private static final EventDebouncer debouncer = new EventDebouncer();

    private enum State { IDLE, NIGHT_STARTED, COMPLETED }
    private State state = State.IDLE;
    private boolean sleptThisNight = false;

    public void onClientTick(Minecraft client) {
        if (client.level == null || client.player == null || state == State.COMPLETED) {
            return;
        }

        // Only track in overworld
        if (!client.level.dimension().identifier().getPath().equals("overworld")) {
            return;
        }

        long timeOfDay = client.level.getDayTime() % 24000;
        boolean isNight = timeOfDay >= 13000 && timeOfDay < 23000;
        boolean isDawn = timeOfDay >= 0 && timeOfDay < 1000;

        switch (state) {
            case IDLE:
                if (isNight) {
                    state = State.NIGHT_STARTED;
                    sleptThisNight = false;
                }
                break;

            case NIGHT_STARTED:
                if (client.player.isSleeping()) {
                    sleptThisNight = true;
                }

                // Check if night has ended and it's dawn
                if (!isNight && isDawn) {
                    if (!sleptThisNight && client.player.isAlive()) {
                        triggerMilestone();
                    }
                    state = State.IDLE;
                }
                break;

            case COMPLETED:
                // Already completed, do nothing
                break;
        }
    }

    private void triggerMilestone() {
        // Very long debounce (24 hours) to prevent repeat triggering
        if (debouncer.shouldTrigger("first_night_survived", 86400000)) {
            state = State.COMPLETED;
            EventPacket packet = new EventPacket("first_night_survived");
            NetworkManager.getInstance().sendEvent(packet);
        }
    }

    /**
     * Called when player sleeps (from SleepListener).
     * Marks that the player slept during the current night cycle.
     */
    public void onPlayerSlept() {
        sleptThisNight = true;
    }

    /**
     * Resets the tracker state (e.g., when player changes worlds).
     */
    public void reset() {
        state = State.IDLE;
        sleptThisNight = false;
    }
}
