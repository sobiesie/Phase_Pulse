package com.phasepal.phasepulse.event.world;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks structure discovery using client-visible chunk structure references.
 */
public class StructureTracker {
	private static final int SAMPLE_INTERVAL = 60;

	private static final Set<String> DANGEROUS_STRUCTURES = Set.of(
			"ocean_monument",
			"nether_fortress",
			"woodland_mansion",
			"ancient_city",
			"bastion_remnant",
			"stronghold",
			"trial_chambers"
	);

	private final EventDebouncer debouncer = new EventDebouncer();
	private final Set<String> discoveredStructures = new HashSet<>();
	private int tickCounter = 0;
	private String lastStructure = null;

	public void onClientTick(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}

		tickCounter++;
		if (tickCounter < SAMPLE_INTERVAL) {
			return;
		}
		tickCounter = 0;

		try {
			detectStructureFromChunk(client);
		} catch (Exception ignored) {
			// Structure data can be absent or inconsistent on client chunks.
		}
	}

	private void detectStructureFromChunk(Minecraft client) {
		LevelChunk chunk = client.level.getChunkAt(client.player.blockPosition());
		if (chunk == null) {
			return;
		}

		Map<Structure, LongSet> structureReferences = chunk.getAllReferences();
		if (structureReferences.isEmpty()) {
			lastStructure = null;
			return;
		}

		var structureRegistry = client.level.registryAccess().registryOrThrow(Registries.STRUCTURE);

		for (Map.Entry<Structure, LongSet> entry : structureReferences.entrySet()) {
			LongSet references = entry.getValue();
			if (references == null || references.isEmpty()) {
				continue;
			}

			ResourceLocation identifier = structureRegistry.getKey(entry.getKey());
			String structureId = identifier != null ? identifier.toString() : "unknown";
			String friendlyName = simplifyStructureId(structureId);

			if (friendlyName.equals(lastStructure)) {
				continue;
			}

			lastStructure = friendlyName;

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

			return;
		}

		lastStructure = null;
	}

	private String simplifyStructureId(String structureId) {
		String simple = structureId.replace("minecraft:", "");

		if (simple.startsWith("village_")) {
			return "village";
		}
		if (simple.startsWith("ruined_portal")) {
			return "ruined_portal";
		}
		if (simple.startsWith("ocean_ruin")) {
			return "ocean_ruins";
		}
		if (simple.startsWith("shipwreck")) {
			return "shipwreck";
		}
		if (simple.startsWith("mineshaft")) {
			return "mineshaft";
		}

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
