package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Listens for player death events on the client side.
 * Detects death by monitoring the player's isDeadOrDying() state transition.
 */
public class DeathListener {
    private final EventDebouncer debouncer = new EventDebouncer();
    private boolean wasDead = false;

    public void onClientTick(Minecraft client) {
        if (client.player == null) {
            wasDead = false;
            return;
        }

        boolean isDead = client.player.isDeadOrDying();

        // Detect transition from alive to dead
        if (isDead && !wasDead) {
            if (debouncer.shouldTrigger("player_death")) {
                String deathCause = "unknown";

                DamageSource recentDamage = client.player.getLastDamageSource();
                if (recentDamage != null) {
                    deathCause = recentDamage.getMsgId();
                }

                EventPacket packet = new EventPacket("player_death")
                        .addMetadata("cause", deathCause);

                NetworkManager.getInstance().sendEvent(packet);
            }
        }

        wasDead = isDead;
    }
}
