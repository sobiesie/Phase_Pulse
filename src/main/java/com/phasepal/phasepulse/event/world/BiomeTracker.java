package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks biome discovery and changes.
 * Sends biome_discovery when player enters a new biome for the first time.
 * Sends biome_changed every time player changes biome.
 * Samples biome every 20 ticks (1 second) to reduce overhead.
 * Uses hysteresis to prevent spam when player is on biome boundaries.
 */
public class BiomeTracker {
    private static final int SAMPLE_INTERVAL = 20; // Ticks between samples
    private static final int HYSTERESIS_COUNT = 10; // Consecutive checks needed to confirm biome change
    private static final long BIOME_CHANGE_COOLDOWN_MS = 15000; // Minimum time between biome_changed events

    private final EventDebouncer debouncer = new EventDebouncer();
    private final Set<String> discoveredBiomes = new HashSet<>();
    private int tickCounter = 0;
    private String lastBiome = null;
    private String pendingBiome = null; // Biome we're potentially changing to
    private int pendingCount = 0; // How many consecutive checks in pendingBiome

    public void onClientTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        // Only sample every SAMPLE_INTERVAL ticks
        tickCounter++;
        if (tickCounter < SAMPLE_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Get current biome
        RegistryEntry<Biome> biomeEntry = client.world.getBiome(client.player.getBlockPos());
        String biomeName = biomeEntry.getKey()
                .map(key -> key.getValue().toString())
                .orElse("unknown");

        // If we're in the same biome as confirmed, nothing to do
        if (biomeName.equals(lastBiome)) {
            // Reset pending state since we're back in confirmed biome
            pendingBiome = null;
            pendingCount = 0;
            return;
        }

        // We're in a different biome than confirmed - use hysteresis
        if (biomeName.equals(pendingBiome)) {
            // Same as pending biome, increment counter
            pendingCount++;
        } else {
            // Different biome, start new pending
            pendingBiome = biomeName;
            pendingCount = 1;
        }

        // Only confirm biome change after HYSTERESIS_COUNT consecutive checks
        if (pendingCount >= HYSTERESIS_COUNT) {
            String previousBiome = lastBiome;
            lastBiome = biomeName;
            pendingBiome = null;
            pendingCount = 0;

            // Send biome_changed event with longer cooldown to prevent spam
            if (previousBiome != null && debouncer.shouldTrigger("biome_changed", BIOME_CHANGE_COOLDOWN_MS)) {
                EventPacket changePacket = new EventPacket("biome_changed")
                        .addMetadata("from", previousBiome)
                        .addMetadata("to", biomeName);

                NetworkManager.getInstance().sendEvent(changePacket);
            }

            // Send biome_discovery event if this is the first time discovering this biome
            if (!discoveredBiomes.contains(biomeName)) {
                discoveredBiomes.add(biomeName);

                if (debouncer.shouldTrigger("biome_discovery_" + biomeName)) {
                    EventPacket discoveryPacket = new EventPacket("biome_discovery")
                            .addMetadata("biome", biomeName);

                    NetworkManager.getInstance().sendEvent(discoveryPacket);
                }
            }
        }
    }
}
