package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Listens for player death events using Fabric API.
 */
public class DeathListener {
    private final EventDebouncer debouncer = new EventDebouncer();

    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof PlayerEntity && entity.getEntityWorld().isClient()) {
                if (debouncer.shouldTrigger("player_death")) {
                    String deathCause = damageSource.getName();

                    EventPacket packet = new EventPacket("player_death")
                            .addMetadata("cause", deathCause);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
        });
    }
}
