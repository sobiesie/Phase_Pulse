package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.Structure;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks structure discovery.
 * Note: Full structure detection requires server-side data.
 * This tracker uses chunk-based heuristics for client-side detection.
 * For single-player, structures are detected via integrated server.
 */
public class StructureTracker {
    private static final int SAMPLE_INTERVAL = 60; // Ticks between samples (3 seconds)

    private final EventDebouncer debouncer = new EventDebouncer();
    private final Set<String> discoveredStructures = new HashSet<>();
    private int tickCounter = 0;
    private String lastStructure = null;

    // Structures that are particularly notable/dangerous
    private static final Set<String> DANGEROUS_STRUCTURES = Set.of(
            "ocean_monument",
            "nether_fortress",
            "woodland_mansion",
            "ancient_city",
            "bastion_remnant",
            "stronghold",
            "trial_chambers"
    );

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

        // Structure detection is limited on client-side
        try {
            detectStructureFromChunk(client);
        } catch (Exception e) {
            // Silently ignore - structure detection may fail
        }
    }

    /**
     * Attempts to detect structures using available client data.
     * This is limited but can work for some cases.
     */
    private void detectStructureFromChunk(MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();

        // Get the current chunk
        Chunk chunk = client.world.getChunk(playerPos);
        if (chunk == null) {
            return;
        }

        // Check chunk structure references (if available)
        var structureReferences = chunk.getStructureReferences();
        if (structureReferences.isEmpty()) {
            lastStructure = null;
            return;
        }

        // Find structures in this chunk
        for (var entry : structureReferences.entrySet()) {
            Structure structure = entry.getKey();
            var references = entry.getValue();

            if (references != null && !references.isEmpty()) {
                // Try to get the structure ID from the registry
                var structureRegistry = client.world.getRegistryManager().get(Registry.STRUCTURE_KEY);
                var identifier = structureRegistry.getId(structure);
                String structureId = identifier != null ? identifier.toString() : "unknown";

                String friendlyName = simplifyStructureId(structureId);

                if (friendlyName.equals(lastStructure)) {
                    continue; // Already in this structure
                }

                lastStructure = friendlyName;

                // Check if this is a new discovery
                if (!discoveredStructures.contains(friendlyName)) {
                    discoveredStructures.add(friendlyName);

                    if (debouncer.shouldTrigger("structure_discovery_" + friendlyName)) {
                        boolean isDangerous = DANGEROUS_STRUCTURES.contains(friendlyName);

                        EventPacket packet = new EventPacket("structure_discovery")
                                .addMetadata("structure", friendlyName)
                                .addMetadata("is_dangerous", isDangerous);

                        NetworkManager.getInstance().sendEvent(packet);
                    }
                }
                return; // Found a structure, stop searching
            }
        }
    }

    /**
     * Simplifies a structure ID for display.
     * e.g., "minecraft:village_plains" -> "village"
     */
    private String simplifyStructureId(String structureId) {
        String simple = structureId.replace("minecraft:", "");

        // Group village variants
        if (simple.startsWith("village_")) {
            return "village";
        }

        // Group ruined portal variants
        if (simple.startsWith("ruined_portal")) {
            return "ruined_portal";
        }

        // Group ocean ruin variants
        if (simple.startsWith("ocean_ruin")) {
            return "ocean_ruins";
        }

        // Group shipwreck variants
        if (simple.startsWith("shipwreck")) {
            return "shipwreck";
        }

        // Group mineshaft variants
        if (simple.startsWith("mineshaft")) {
            return "mineshaft";
        }

        // Rename some structures
        return switch (simple) {
            case "desert_pyramid" -> "desert_temple";
            case "jungle_pyramid" -> "jungle_temple";
            case "swamp_hut" -> "witch_hut";
            case "fortress" -> "nether_fortress";
            case "mansion" -> "woodland_mansion";
            default -> simple;
        };
    }
}
