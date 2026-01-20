package com.phasepal.phasepulse.mixin;

import com.mojang.datafixers.util.Either;
import com.phasepal.phasepulse.event.player.SleepListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when sleep fails, particularly due to monsters nearby.
 * Hooks into PlayerEntity.trySleep to capture the failure reason.
 */
@Mixin(PlayerEntity.class)
public class SleepMixin {

    @Inject(method = "trySleep", at = @At("RETURN"))
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Only process on client side
        if (!player.getEntityWorld().isClient()) {
            return;
        }

        Either<SleepFailureReason, Unit> result = cir.getReturnValue();

        // Check if sleep failed (left = failure, right = success)
        result.ifLeft(reason -> {
            SleepListener.onSleepFailed(reason);
        });
    }
}
