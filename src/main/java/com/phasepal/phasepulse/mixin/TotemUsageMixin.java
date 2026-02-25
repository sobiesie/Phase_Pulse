package com.phasepal.phasepulse.mixin;

import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect when a player uses a Totem of Undying.
 */
@Mixin(LivingEntity.class)
public class TotemUsageMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        // Status 35 is used for the Totem of Undying effect.
        if (status == 35) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof Player player && player == Minecraft.getInstance().player && player.level().isClientSide()) {
                EventPacket packet = new EventPacket("totem_used");
                NetworkManager.getInstance().sendEvent(packet);
            }
        }
    }
}
