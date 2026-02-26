package com.phasepal.phasepulse;

import com.phasepal.phasepulse.event.EventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PhasePulse.MOD_ID)
public class PhasePulse {
	public static final String MOD_ID = "phase_pulse";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public PhasePulse() {
		PhasePulseClient.initialize();
		TickEvent.ClientTickEvent.Post.BUS.addListener(this::onClientTickPost);
	}

	private void onClientTickPost(TickEvent.ClientTickEvent.Post event) {
		EventRegistry.onClientTick(Minecraft.getInstance());
	}
}
