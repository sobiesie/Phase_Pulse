package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

    // Harmful effects available in 1.20.4
    private static final Set<StatusEffect> BASE_HARMFUL_EFFECTS = Set.of(
            StatusEffects.POISON,
            StatusEffects.WITHER,
            StatusEffects.HUNGER,
            StatusEffects.MINING_FATIGUE,
            StatusEffects.WEAKNESS,
            StatusEffects.BLINDNESS,
            StatusEffects.NAUSEA,
            StatusEffects.SLOWNESS,
            StatusEffects.LEVITATION,
            StatusEffects.UNLUCK,
            StatusEffects.DARKNESS
    );

    // 1.21+ effects: checked by id so code still compiles on 1.20.4
    private static final Set<String> OPTIONAL_121_HARMFUL_EFFECTS = Set.of(
            "infested",
            "oozing",
            "weaving",
            "wind_charged"
    );

    public void onClientTick(MinecraftClient client) {
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
        for (StatusEffectInstance effectInstance : client.player.getStatusEffects()) {
            StatusEffect effect = effectInstance.getEffectType();
            String effectName = getEffectName(effect);

            if (isTrackedHarmfulEffect(effect, effectName)) {
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
                                .addMetadata("is_damaging", isDamagingEffect(effectName));

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
    private String getEffectName(StatusEffect effect) {
        Identifier id = Registry.STATUS_EFFECT.getId(effect);
        if (id == null) {
            return "unknown";
        }
        return id.toString().replace("minecraft:", "");
    }

    private boolean isTrackedHarmfulEffect(StatusEffect effect, String effectName) {
        return BASE_HARMFUL_EFFECTS.contains(effect) || OPTIONAL_121_HARMFUL_EFFECTS.contains(effectName);
    }

    /**
     * Checks if the effect causes direct damage to the player.
     */
    private boolean isDamagingEffect(String effectName) {
        return "poison".equals(effectName) || "wither".equals(effectName);
    }
}
