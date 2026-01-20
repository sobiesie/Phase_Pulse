package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TamingListener;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when horse-like animals are tamed.
 * Covers: horse, donkey, mule, llama, camel, etc.
 */
@Mixin(AbstractHorseEntity.class)
public class HorseTamingMixin {

    @Inject(method = "bondWithPlayer", at = @At("HEAD"))
    private void onBondWithPlayer(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorseEntity entity = (AbstractHorseEntity) (Object) this;

        // Only trigger on client side
        if (entity.getEntityWorld().isClient()) {
            String entityType = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            TamingListener.onHorseTamed(entityType);
        }
    }
}
