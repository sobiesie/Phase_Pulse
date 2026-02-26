package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashSet;
import java.util.Set;

/**
 * Monitors player for harmful status effects like Poison, Wither, etc.
 * Sends events when harmful effects are applied to the player.
 */
public class StatusEffectListener {
    private static final int SCAN_INTERVAL = 20; // Check every second (20 ticks)

    private final EventDebouncer debouncer = new EventDebouncer();
    private final Set<String> activeHarmfulEffects = new HashSet<>();
    private int tickCounter = 0;

    // Track by namespaced IDs so mapping differences do not break detection.
    private static final Set<String> HARMFUL_EFFECTS = Set.of(
            "minecraft:poison",
            "minecraft:wither",
            "minecraft:hunger",
            "minecraft:mining_fatigue",
            "minecraft:weakness",
            "minecraft:blindness",
            "minecraft:nausea",
            "minecraft:slowness",
            "minecraft:levitation",
            "minecraft:unluck",
            "minecraft:darkness",
            "minecraft:infested",
            "minecraft:oozing",
            "minecraft:weaving",
            "minecraft:wind_charged"
    );

    public void onClientTick(Minecraft client) {
        if (client.player == null) {
            return;
        }

        // Only scan every SCAN_INTERVAL ticks
        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Track which effects are currently active
        Set<String> currentEffects = new HashSet<>();

        // Check all active status effects
        for (MobEffectInstance effectInstance : client.player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (effectId == null) {
                continue;
            }

            String effectIdString = effectId.toString();
            if (HARMFUL_EFFECTS.contains(effectIdString)) {
                String effectName = getEffectName(effectIdString);
                currentEffects.add(effectName);

                // Only send event if this is a new effect (not already active)
                if (!activeHarmfulEffects.contains(effectName)) {
                    // Debounce per effect type (30 second cooldown per effect)
                    if (debouncer.shouldTrigger("harmful_effect_" + effectName, 30000)) {
                        int amplifier = effectInstance.getAmplifier();
                        int durationTicks = effectInstance.getDuration();
                        int durationSeconds = durationTicks / 20;

                        EventPacket packet = new EventPacket("harmful_effect")
                                .addMetadata("effect", effectName)
                                .addMetadata("amplifier", amplifier)
                                .addMetadata("duration_seconds", durationSeconds)
                                .addMetadata("is_damaging", isDamagingEffect(effectIdString));

                        NetworkManager.getInstance().sendEvent(packet);
                    }
                }
            }
        }

        // Update the set of active effects
        activeHarmfulEffects.clear();
        activeHarmfulEffects.addAll(currentEffects);
    }

    /**
     * Gets a clean effect name from the registry entry.
     */
    private String getEffectName(String effectId) {
        return effectId.replace("minecraft:", "");
    }

    /**
     * Checks if the effect causes direct damage to the player.
     */
    private boolean isDamagingEffect(String effectId) {
        return "minecraft:poison".equals(effectId) || "minecraft:wither".equals(effectId);
    }
}
