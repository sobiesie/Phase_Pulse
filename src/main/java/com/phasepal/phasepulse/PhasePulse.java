package com.phasepal.phasepulse;

import com.phasepal.phasepulse.event.EventRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PhasePulse.MOD_ID)
public class PhasePulse {
	public static final String MOD_ID = "phase_pulse";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static boolean initialized = false;

	public PhasePulse() {
		LOGGER.info("PhasePulse mod loaded");
		NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
	}

	private void onClientTickPost(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		if (!initialized) {
			initialized = true;
			PhasePulseClient.initialize();
		}

		EventRegistry.onClientTick(Minecraft.getInstance());
	}
}
