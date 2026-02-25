package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import com.phasepal.phasepulse.event.combat.MobKilledListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when the player attacks an entity.
 * Used to sustain combat state and track entities for kill events.
 */
@Mixin(Player.class)
public class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Only process for the local player on the client
        if (player == Minecraft.getInstance().player && player.level().isClientSide()) {
            // Sustain combat state
            if (target instanceof Monster) {
                CombatTracker tracker = EventRegistry.getCombatTracker();
                if (tracker != null) {
                    tracker.onCombatActivity();
                }
            }
            
            // Track for kill event
            MobKilledListener.onPlayerAttackedEntity(target);
        }
    }
}
