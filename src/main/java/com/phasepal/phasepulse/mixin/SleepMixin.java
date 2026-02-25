package com.phasepal.phasepulse.mixin;

import com.mojang.datafixers.util.Either;
import com.phasepal.phasepulse.event.player.SleepListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.util.Unit;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect when sleep fails, particularly due to monsters nearby.
 * Hooks into Player.trySleep to capture the failure reason.
 */
@Mixin(Player.class)
public class SleepMixin {

    @Inject(method = "startSleepInBed", at = @At("RETURN"))
    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> cir) {
        Player player = (Player) (Object) this;

        // Only process if it is the local player and on client side
        if (player != Minecraft.getInstance().player) {
            return;
        }

        Either<BedSleepingProblem, Unit> result = cir.getReturnValue();

        // Check if sleep failed (left = failure, right = success)
        result.ifLeft(reason -> {
            SleepListener.onSleepFailed(reason);
        });
    }
}
