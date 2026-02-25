package com.phasepal.phasepulse;

import com.phasepal.phasepulse.event.EventRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PhasePulse.MOD_ID)
public class PhasePulse {
	public static final String MOD_ID = "phase_pulse";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public PhasePulse(IEventBus modEventBus) {
		modEventBus.addListener(this::onClientSetup);
		NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(PhasePulseClient::initialize);
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		EventRegistry.onClientTick(Minecraft.getInstance());
	}
}
