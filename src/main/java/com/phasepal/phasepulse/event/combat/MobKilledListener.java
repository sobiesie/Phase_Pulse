package com.phasepal.phasepulse.event.combat;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Listens for mob killed events on the client side.
 * Tracks entities attacked by the player and detects when they die.
 */
public class MobKilledListener {
    private static final double TRACKING_RANGE = 16.0;
    private static final long TRACK_TIMEOUT_MS = 10000; // Stop tracking after 10 seconds

    private final EventDebouncer debouncer = new EventDebouncer();

    // Track entities that have been attacked by player: entity ID -> last attack time
    private final Map<Integer, Long> attackedEntities = new HashMap<>();
    // Track entities that were alive last tick: entity ID -> was alive
    private final Map<Integer, Boolean> entityAliveState = new HashMap<>();

    /**
     * Called when the player attacks an entity (from HurtListener or attack event).
     */
    public void onPlayerAttackedEntity(Entity target) {
        if (target instanceof LivingEntity && !(target instanceof PlayerEntity)) {
            attackedEntities.put(target.getId(), System.currentTimeMillis());
        }
    }

    public void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        long now = System.currentTimeMillis();

        // Clean up old tracked entities
        Iterator<Map.Entry<Integer, Long>> iterator = attackedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            if (now - entry.getValue() > TRACK_TIMEOUT_MS) {
                iterator.remove();
                entityAliveState.remove(entry.getKey());
            }
        }

        // Check nearby entities for death
        Box searchBox = client.player.getBoundingBox().expand(TRACKING_RANGE);
        for (Entity entity : client.world.getOtherEntities(client.player, searchBox)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (entity instanceof PlayerEntity) {
                continue;
            }

            int entityId = entity.getId();
            boolean isAlive = living.isAlive();
            Boolean wasAlive = entityAliveState.get(entityId);

            // Detect death transition for entities we attacked
            if (wasAlive != null && wasAlive && !isAlive && attackedEntities.containsKey(entityId)) {
                // Entity just died and we attacked it
                String mobType = getMobTypeName(living);

                if (debouncer.shouldTrigger("mob_killed_" + mobType, 500)) {
                    EventPacket packet = new EventPacket("mob_killed")
                            .addMetadata("mob_type", mobType)
                            .addMetadata("is_hostile", living instanceof Monster)
                            .addMetadata("is_animal", living instanceof AnimalEntity);

                    NetworkManager.getInstance().sendEvent(packet);
                }

                // Stop tracking this entity
                attackedEntities.remove(entityId);
                entityAliveState.remove(entityId);
            } else {
                // Update alive state for tracked entities
                if (attackedEntities.containsKey(entityId)) {
                    entityAliveState.put(entityId, isAlive);
                }
            }
        }

        // Also track attack via player's attack target
        Entity attackTarget = client.player.getAttacking();
        if (attackTarget instanceof LivingEntity && !(attackTarget instanceof PlayerEntity)) {
            attackedEntities.put(attackTarget.getId(), now);
        }
    }

    private String getMobTypeName(LivingEntity entity) {
        // Get a clean mob type name
        String typeName = entity.getType().toString();
        // Extract just the entity name from "entity.minecraft.zombie" format
        if (typeName.contains(".")) {
            String[] parts = typeName.split("\\.");
            typeName = parts[parts.length - 1];
        }
        return typeName;
    }
}
