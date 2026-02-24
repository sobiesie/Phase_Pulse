package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.FishingListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when a player catches something with a fishing rod.
 */
@Mixin(FishingBobberEntity.class)
public class FishingMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;pullHookedEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onPullInEntity(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        // This is called when an entity (like a fish item) is pulled in.
        // However, on the client, we might not have all the info.
        // For now, we'll signal a catch event if we are the owner.
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;
        PlayerEntity owner = bobber.getPlayerOwner();
        
        if (owner != null && owner == MinecraftClient.getInstance().player && owner.getEntityWorld().isClient()) {
            // We'll send a placeholder item for now, or just signal the catch
            FishingListener.onItemCaught(usedItem);
        }
    }
}
