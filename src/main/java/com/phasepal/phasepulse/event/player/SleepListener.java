package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.world.entity.player.Player;

/**
 * NeoForge port placeholder for sleep lifecycle events.
 * Sleep start/stop hooks will be wired through NeoForge events.
 */
public class SleepListener {
    private static final EventDebouncer staticDebouncer = new EventDebouncer();

    public void register() {
        // TODO: Wire sleep start/stop lifecycle via NeoForge events.
    }

    /**
     * Called from SleepMixin when a sleep attempt fails.
     * @param reason The reason sleep failed
     */
    public static void onSleepFailed(Player.BedSleepingProblem reason) {
        // Debounce to prevent spam (3 second cooldown for sleep failures)
        if (!staticDebouncer.shouldTrigger("sleep_failed", 3000)) {
            return;
        }

        String reasonString = reason.toString().toLowerCase();
        boolean isMonstersNearby = reasonString.contains("safe");
        String readableReason = reasonString.contains("safe") ? "monsters_nearby" : reasonString;

        EventPacket packet = new EventPacket("sleep_failed")
                .addMetadata("reason", readableReason)
                .addMetadata("monsters_nearby", isMonstersNearby);

        NetworkManager.getInstance().sendEvent(packet);
    }
}
