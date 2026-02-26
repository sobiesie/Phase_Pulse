package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.TamingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when horse-like animals are tamed.
 * Covers: horse, donkey, mule, llama, camel, etc.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.horse.AbstractHorse")
public class HorseTamingMixin {

    @Inject(method = "tameWithName", at = @At("HEAD"))
    private void onBondWithPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        // Only trigger if bonding with the local player and on client side
        if (player == Minecraft.getInstance().player && entity.level().isClientSide()) {
            var entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (entityType != null) {
                TamingListener.onHorseTamed(entityType.toString());
            }
        }
    }
}
