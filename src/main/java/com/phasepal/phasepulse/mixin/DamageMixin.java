package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.EventRegistry;
import com.phasepal.phasepulse.event.combat.CombatTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when the player takes damage for combat tracking.
 */
@Mixin(PlayerEntity.class)
public class DamageMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Only track client-side damage
        if (player.getEntityWorld().isClient() && amount > 0) {
            CombatTracker tracker = EventRegistry.getCombatTracker();
            if (tracker != null) {
                tracker.onPlayerDamaged();
            }
        }
    }
}
