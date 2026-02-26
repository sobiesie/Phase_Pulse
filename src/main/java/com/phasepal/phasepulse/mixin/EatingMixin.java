package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.EatingListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a living entity consumes an item (eating/drinking).
 * Injects at the HEAD of LivingEntity.completeUsingItem() before the active item is cleared.
 */
@Mixin(LivingEntity.class)
public class EatingMixin {
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void onConsumeItem(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Get the item being used from the entity's active hand
        ItemStack stack = entity.getUseItem();

        if (stack != null && !stack.isEmpty()) {
            EatingListener.onItemConsumed(entity, stack);
        }
    }
}
