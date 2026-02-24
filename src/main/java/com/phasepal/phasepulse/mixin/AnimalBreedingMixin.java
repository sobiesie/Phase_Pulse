package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.BreedingListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when animals breed.
 */
@Mixin(AnimalEntity.class)
public class AnimalBreedingMixin {
    @Inject(method = "breed", at = @At("HEAD"))
    private void onBreed(ServerWorld world, AnimalEntity other, CallbackInfo ci) {
        // Breeding typically happens on the server.
        // If we want to detect it on the client, we might need a different hook
        // or ensure this mixin is only used when the mod can see the event.
        // However, this is a good start to ensure we catch the logic.
        AnimalEntity parent = (AnimalEntity) (Object) this;
        
        // Only trigger if this is happening on the client world (if possible)
        // or for the local player's context if we could identify them.
        if (parent.getEntityWorld().isClient()) {
            BreedingListener.onAnimalBred(parent, other);
        }
    }
}
