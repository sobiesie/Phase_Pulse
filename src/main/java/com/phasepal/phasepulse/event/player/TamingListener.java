package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;

/**
 * Handles animal taming events.
 * Called from TamingMixin when an animal is successfully tamed.
 */
public class TamingListener {

    private static final EventDebouncer debouncer = new EventDebouncer();

    // Map entity types to friendly names
    private static final Map<String, String> ANIMAL_NAMES = Map.of(
            "minecraft:wolf", "wolf",
            "minecraft:cat", "cat",
            "minecraft:parrot", "parrot",
            "minecraft:horse", "horse",
            "minecraft:donkey", "donkey",
            "minecraft:mule", "mule",
            "minecraft:llama", "llama",
            "minecraft:trader_llama", "llama",
            "minecraft:axolotl", "axolotl",
            "minecraft:camel", "camel"
    );

    // Special messages for certain animals
    private static final Map<String, String> ANIMAL_TRAITS = Map.of(
            "wolf", "companion",      // Combat companion
            "cat", "utility",         // Creeper repellent, gifts
            "parrot", "companion",    // Shoulder buddy
            "horse", "mount",         // Speed/travel
            "donkey", "mount",        // Storage mount
            "mule", "mount",          // Storage mount
            "llama", "utility",       // Caravan/storage
            "axolotl", "companion",   // Aquatic helper
            "camel", "mount"          // Two-player mount
    );

    /**
     * Called from TamingMixin when an animal is tamed.
     * @param entity The tamed entity
     */
    public static void onAnimalTamed(TamableAnimal entity) {
        if (!entity.level().isClientSide()) {
            return;
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        String animalName = ANIMAL_NAMES.getOrDefault(entityId, entityId.replace("minecraft:", ""));
        String trait = ANIMAL_TRAITS.getOrDefault(animalName, "companion");

        // Debounce per animal type (10 second cooldown)
        if (!debouncer.shouldTrigger("animal_tamed_" + animalName, 10000)) {
            return;
        }

        EventPacket packet = new EventPacket("animal_tamed")
                .addMetadata("animal", animalName)
                .addMetadata("trait", trait);

        NetworkManager.getInstance().sendEvent(packet);
    }

    /**
     * Called when a horse-like entity is tamed (horses, donkeys, etc. use different taming).
     * @param entityType The type of entity tamed
     */
    public static void onHorseTamed(String entityType) {
        String animalName = ANIMAL_NAMES.getOrDefault(entityType, entityType.replace("minecraft:", ""));
        String trait = ANIMAL_TRAITS.getOrDefault(animalName, "mount");

        // Debounce per animal type (10 second cooldown)
        if (!debouncer.shouldTrigger("animal_tamed_" + animalName, 10000)) {
            return;
        }

        EventPacket packet = new EventPacket("animal_tamed")
                .addMetadata("animal", animalName)
                .addMetadata("trait", trait);

        NetworkManager.getInstance().sendEvent(packet);
    }
}
