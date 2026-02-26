package com.phasepal.phasepulse;

import com.phasepal.phasepulse.event.EventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PhasePulse.MOD_ID)
public class PhasePulse {
	public static final String MOD_ID = "phase_pulse";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public PhasePulse() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onClientSetup);
		MinecraftForge.EVENT_BUS.addListener(this::onClientTickPost);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(PhasePulseClient::initialize);
	}

	private void onClientTickPost(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		EventRegistry.onClientTick(Minecraft.getInstance());
	}
}
