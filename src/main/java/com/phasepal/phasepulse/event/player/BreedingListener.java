package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Handles animal breeding events.
 * Called from AnimalEntityMixin when animals are successfully bred.
 */
public class BreedingListener {
    private static final EventDebouncer debouncer = new EventDebouncer();

    /**
     * Called when two animals breed.
     * @param parent1 The first parent
     * @param parent2 The second parent
     */
    public static void onAnimalBred(Animal parent1, Animal parent2) {
        // Only track for the local player on the client side
        // Note: Breeding usually happens on the server, but some events or mixins might trigger on client
        if (parent1.level().isClientSide()) {
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(parent1.getType()).toString().replace("minecraft:", "");
            
            // Debounce per animal type (10 second cooldown)
            if (!debouncer.shouldTrigger("animal_bred_" + entityId, 10000)) {
                return;
            }

            EventPacket packet = new EventPacket("animal_bred")
                    .addMetadata("animal", entityId);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }
}
