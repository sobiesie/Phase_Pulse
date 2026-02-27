package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.gui.components.toasts.AdvancementToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Listens for advancement (achievement) earned events.
 * Called from AdvancementMixin when player earns an advancement.
 */
public class AdvancementListener {
	private static final EventDebouncer debouncer = new EventDebouncer();
	private static Field advancementField;

	static {
		// 1.20.2 and newer lines use different advancement wrappers.
		// Detect by shape to keep compatibility across branch targets.
		try {
			for (Field field : AdvancementToast.class.getDeclaredFields()) {
				if (looksLikeAdvancementType(field.getType())) {
					field.setAccessible(true);
					advancementField = field;
					break;
				}
			}
		} catch (Exception e) {
			PhasePulse.LOGGER.error("Failed to inspect AdvancementToast fields", e);
		}
	}

	/**
	 * Called when an advancement toast is shown (client-side).
	 * @param toast The advancement toast
	 */
	public static void onAdvancementToastShown(AdvancementToast toast) {
		Object advancement = extractAdvancementObject(toast);
		if (advancement != null) {
			onAdvancementEarned(advancement);
		}
	}

	/**
	 * Called when player earns an advancement.
	 * @param advancement The advancement object from toast internals
	 */
	public static void onAdvancementEarned(Object advancement) {
		String advancementId = extractAdvancementId(advancement);
		if (advancementId == null || advancementId.isBlank()) {
			return;
		}

		if (advancementId.contains("recipes/")) {
			return;
		}

		if (debouncer.shouldTrigger("achievement_" + advancementId)) {
			EventPacket packet = new EventPacket("achievement_earned")
					.addMetadata("achievement", advancementId);
			NetworkManager.getInstance().sendEvent(packet);
		}
	}

	private static Object extractAdvancementObject(AdvancementToast toast) {
		try {
			if (advancementField != null) {
				return advancementField.get(toast);
			}

			for (Field field : toast.getClass().getDeclaredFields()) {
				if (looksLikeAdvancementType(field.getType())) {
					field.setAccessible(true);
					advancementField = field;
					return field.get(toast);
				}
			}
		} catch (Exception e) {
			PhasePulse.LOGGER.error("Failed to extract advancement from toast", e);
		}

		return null;
	}

	private static boolean looksLikeAdvancementType(Class<?> type) {
		if (type == null) {
			return false;
		}

		String simpleName = type.getSimpleName();
		if (!simpleName.contains("Advancement")) {
			return false;
		}

		return hasNoArgMethod(type, "id") || hasNoArgMethod(type, "getId");
	}

	private static String extractAdvancementId(Object advancement) {
		Object idValue = invokeNoArg(advancement, "id");
		if (idValue == null) {
			idValue = invokeNoArg(advancement, "getId");
		}

		return idValue != null ? idValue.toString() : null;
	}

	private static boolean hasNoArgMethod(Class<?> type, String name) {
		try {
			type.getMethod(name);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	private static Object invokeNoArg(Object target, String name) {
		if (target == null) {
			return null;
		}

		try {
			Method method = target.getClass().getMethod(name);
			return method.invoke(target);
		} catch (Exception ignored) {
			return null;
		}
	}
}
