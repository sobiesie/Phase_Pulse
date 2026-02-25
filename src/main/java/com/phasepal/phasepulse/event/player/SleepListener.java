package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Tracks local player sleep state from client ticks.
 * Sleep failure is still captured via SleepMixin.
 */
public class SleepListener {
	private static final EventDebouncer staticDebouncer = new EventDebouncer();
	private static final long MORNING_START = 0;
	private static final long MORNING_END = 1000;

	private final EventDebouncer debouncer = new EventDebouncer();
	private boolean wasInBed = false;
	private long sleepStartTime = -1;

	public void register() {
		// No event bus registration needed. This listener is polled from EventRegistry.onClientTick.
	}

	public void onClientTick(Minecraft client) {
		if (client.level == null || client.player == null) {
			wasInBed = false;
			sleepStartTime = -1;
			return;
		}

		long worldTime = client.level.getDayTime() % 24000;
		boolean isSleeping = client.player.isSleeping();

		if (!wasInBed && isSleeping) {
			wasInBed = true;
			sleepStartTime = worldTime;

			if (debouncer.shouldTrigger("player_sleep")) {
				EventPacket packet = new EventPacket("player_sleep")
						.addMetadata("world_time", worldTime);
				NetworkManager.getInstance().sendEvent(packet);
			}
			return;
		}

		if (wasInBed && !isSleeping) {
			boolean isNowMorning = worldTime >= MORNING_START && worldTime <= MORNING_END;
			boolean timeWrapped = sleepStartTime != -1 && worldTime < sleepStartTime;
			boolean actuallySlept = isNowMorning && timeWrapped;

			if (actuallySlept && debouncer.shouldTrigger("player_wake")) {
				EventPacket packet = new EventPacket("player_wake")
						.addMetadata("world_time", worldTime);
				NetworkManager.getInstance().sendEvent(packet);
			}

			wasInBed = false;
			sleepStartTime = -1;
		}
	}

	/**
	 * Called from SleepMixin when a sleep attempt fails.
	 *
	 * @param reason The reason sleep failed
	 */
	public static void onSleepFailed(Player.BedSleepingProblem reason) {
		if (!staticDebouncer.shouldTrigger("sleep_failed", 3000)) {
			return;
		}

		String reasonString = reason.toString().toLowerCase();
		boolean isMonstersNearby = reasonString.contains("safe");
		String readableReason = reasonString.contains("safe") ? "monsters_nearby" : reasonString;

		EventPacket packet = new EventPacket("sleep_failed")
				.addMetadata("reason", readableReason)
				.addMetadata("monsters_nearby", isMonstersNearby);

		NetworkManager.getInstance().sendEvent(packet);
	}
}
