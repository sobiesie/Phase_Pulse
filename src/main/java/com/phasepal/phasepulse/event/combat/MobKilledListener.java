package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Listens for mob killed events (when player kills an entity).
 */
public class MobKilledListener {
    private final EventDebouncer debouncer = new EventDebouncer();

    public void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Check if killed by a player
            if (damageSource.getAttacker() instanceof PlayerEntity && entity.getEntityWorld().isClient()) {
                // Don't track player deaths here (handled by DeathListener)
                if (!(entity instanceof PlayerEntity)) {
                    String mobType = entity.getType().toString();

                    if (debouncer.shouldTrigger("mob_killed_" + mobType, 500)) {
                        EventPacket packet = new EventPacket("mob_killed")
                                .addMetadata("mob_type", mobType);

                        NetworkManager.getInstance().sendEvent(packet);
                    }
                }
            }
        });
    }
}
