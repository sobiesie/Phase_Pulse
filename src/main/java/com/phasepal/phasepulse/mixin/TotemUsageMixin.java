package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player uses a Totem of Undying.
 */
@Mixin(LivingEntity.class)
public class TotemUsageMixin {
    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        // Status 35 is used for the Totem of Undying effect.
        if (status == 35) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof PlayerEntity player && player == MinecraftClient.getInstance().player && player.getEntityWorld().isClient()) {
                EventPacket packet = new EventPacket("totem_used");
                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
