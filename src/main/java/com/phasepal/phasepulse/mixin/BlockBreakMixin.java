package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.event.player.BlockBrokenListener;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when the player breaks a block on the client side.
 * Hooks into ClientPlayerInteractionManager.breakBlock which is called
 * when the client completes mining a block.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class BlockBreakMixin {
    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockBrokenListener.onBlockBroken(pos);
    }
}
