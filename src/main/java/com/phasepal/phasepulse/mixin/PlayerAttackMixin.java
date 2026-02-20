package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when the player attacks an entity.
 * Used to sustain combat state and track entities for kill events.
 */
@Mixin(PlayerEntity.class)
public class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Only process for the local player on the client
        if (player == MinecraftClient.getInstance().player && player.getEntityWorld().isClient()) {
            CombatTracker tracker = EventRegistry.getCombatTracker();
            if (tracker != null) {
                tracker.onCombatActivity();
            }
        }
    }
}
