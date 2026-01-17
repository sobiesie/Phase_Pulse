package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks biome discovery - sends event when player enters a new biome.
 * Samples biome every 20 ticks (1 second) to reduce overhead.
 */
public class BiomeTracker {
    private static final int SAMPLE_INTERVAL = 20; // Ticks between samples

    private final EventDebouncer debouncer = new EventDebouncer();
    private final Set<String> discoveredBiomes = new HashSet<>();
    private int tickCounter = 0;
    private String lastBiome = null;

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

        // Check if this is a new biome
        if (!biomeName.equals(lastBiome)) {
            lastBiome = biomeName;

            // Only send event if this is the first time discovering this biome
            if (!discoveredBiomes.contains(biomeName)) {
                discoveredBiomes.add(biomeName);

                if (debouncer.shouldTrigger("biome_" + biomeName)) {
                    EventPacket packet = new EventPacket("biome_discovery")
                            .addMetadata("biome", biomeName);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }
        }
    }
}
