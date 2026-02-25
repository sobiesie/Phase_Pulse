package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Listens for block placement attempts on the local client player.
 */
public class BlockPlacedListener {
	private final EventDebouncer debouncer = new EventDebouncer();
	private boolean registered = false;

	public void register() {
		if (registered) {
			return;
		}

		NeoForge.EVENT_BUS.addListener(this::onUseItemOnBlock);
		registered = true;
	}

	private void onUseItemOnBlock(UseItemOnBlockEvent event) {
		if (event.getSide() != LogicalSide.CLIENT || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
			return;
		}

		Player player = event.getPlayer();
		Minecraft client = Minecraft.getInstance();
		if (player == null || client.player == null || player != client.player) {
			return;
		}

		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof BlockItem blockItem)) {
			return;
		}

		String blockType = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString().replace("minecraft:", "");

		if (debouncer.shouldTrigger("block_placed", 100)) {
			EventPacket packet = new EventPacket("block_placed")
					.addMetadata("block", blockType);

			NetworkManager.getInstance().sendEvent(packet);
		}
	}
}
