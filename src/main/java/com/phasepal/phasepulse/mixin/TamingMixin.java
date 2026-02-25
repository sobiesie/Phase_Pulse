package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TamingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;

/**
 * Mixin to detect when tameable animals (wolf, cat, parrot) are tamed.
 */
@Mixin(TamableAnimal.class)
public abstract class TamingMixin {

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void onSetOwner(LivingEntity owner, CallbackInfo ci) {
        TamableAnimal entity = (TamableAnimal) (Object) this;

        // Only trigger if being tamed by the local player on the client
        if (owner == Minecraft.getInstance().player && entity.level().isClientSide()) {
            TamingListener.onAnimalTamed(entity);
        }
    }
}
