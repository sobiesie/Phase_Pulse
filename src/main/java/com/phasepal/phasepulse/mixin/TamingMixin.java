package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TamingListener;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when tameable animals (wolf, cat, parrot) are tamed.
 */
@Mixin(TameableEntity.class)
public abstract class TamingMixin {

    @Shadow
    public abstract boolean isTamed();

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void onSetOwner(PlayerEntity owner, CallbackInfo ci) {
        TameableEntity entity = (TameableEntity) (Object) this;

        // Only trigger if being tamed (not untamed) and on client
        if (owner != null && entity.getEntityWorld().isClient() && entity.isTamed()) {
            TamingListener.onAnimalTamed(entity);
        }
    }
}
