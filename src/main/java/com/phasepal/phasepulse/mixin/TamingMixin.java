package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TamingListener;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to detect when tameable animals (wolf, cat, parrot) are tamed.
 */
@Mixin(TameableEntity.class)
public abstract class TamingMixin {

    @Shadow
    public abstract boolean isTamed();

    @Inject(method = "setOwnerUuid", at = @At("TAIL"))
    private void onSetOwner(@Nullable UUID ownerUuid, CallbackInfo ci) {
        TameableEntity entity = (TameableEntity) (Object) this;

        // Only trigger if being tamed (not untamed) and on client
        if (ownerUuid != null && entity.getEntityWorld().isClient() && entity.isTamed()) {
            TamingListener.onAnimalTamed(entity);
        }
    }
}
