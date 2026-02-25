package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.FishingListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when a player catches something with a fishing rod.
 */
@Mixin(FishingHook.class)
public class FishingMixin {
    @Inject(method = "retrieve", at = @At("HEAD"))
    private void onPullInEntity(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        // This is called when an entity (like a fish item) is pulled in.
        // However, on the client, we might not have all the info.
        // For now, we'll signal a catch event if we are the owner.
        FishingHook bobber = (FishingHook) (Object) this;
        Player owner = bobber.getPlayerOwner();
        
        if (owner != null && owner == Minecraft.getInstance().player && owner.level().isClientSide()) {
            // We'll send a placeholder item for now, or just signal the catch
            FishingListener.onItemCaught(usedItem);
        }
    }
}
