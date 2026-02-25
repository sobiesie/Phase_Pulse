package com.phasepal.phasepulse.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when the player takes damage.
 * Note: This mixin targets the server-side damage method.
 * Client-side hurt detection is handled by HurtListener via hurtTime monitoring.
 */
@Mixin(Player.class)
public class DamageMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Server-side damage hook - currently unused as this is a client-side mod.
        // Client-side hurt detection is handled by HurtListener monitoring player.hurtTime
    }
}
