package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.PhasePulse;
import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.toast.AdvancementToast;

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
        // Find the advancement field in AdvancementToast using reflection
        try {
            for (Field field : AdvancementToast.class.getDeclaredFields()) {
                String fieldTypeName = field.getType().getName();
                if (fieldTypeName.equals("net.minecraft.advancement.AdvancementEntry")
                        || fieldTypeName.equals("net.minecraft.advancement.Advancement")) {
                    field.setAccessible(true);
                    advancementField = field;
                    break;
                }
            }
        } catch (Exception e) {
            PhasePulse.LOGGER.error("Failed to find advancement field in AdvancementToast", e);
        }
    }

    /**
     * Called when an advancement toast is shown (client-side).
     * @param toast The advancement toast
     */
    public static void onAdvancementToastShown(AdvancementToast toast) {
        try {
            if (advancementField != null) {
                Object advancement = advancementField.get(toast);
                if (advancement != null) {
                    onAdvancementEarned(advancement);
                }
            }
        } catch (Exception e) {
            PhasePulse.LOGGER.error("Failed to extract advancement from toast", e);
        }
    }

    /**
     * Called when player earns an advancement.
     * @param advancement The advancement earned
     */
    public static void onAdvancementEarned(Object advancement) {
        String advancementId = getAdvancementId(advancement);
        if (advancementId == null || advancementId.isEmpty()) {
            return;
        }

        // Filter out recipe advancements (they're spammy and auto-unlock)
        if (advancementId.contains("recipes/")) {
            return;
        }

        if (debouncer.shouldTrigger("achievement_" + advancementId)) {
            EventPacket packet = new EventPacket("achievement_earned")
                    .addMetadata("achievement", advancementId);

            NetworkManager.getInstance().sendEvent(packet);
        }
    }

    private static String getAdvancementId(Object advancement) {
        try {
            Method idMethod = advancement.getClass().getMethod("id");
            Object idValue = idMethod.invoke(advancement);
            if (idValue != null) {
                return idValue.toString();
            }
        } catch (Exception ignored) {
        }

        try {
            Method getIdMethod = advancement.getClass().getMethod("getId");
            Object idValue = getIdMethod.invoke(advancement);
            if (idValue != null) {
                return idValue.toString();
            }
        } catch (Exception ignored) {
        }

        try {
            Field idField = advancement.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(advancement);
            if (idValue != null) {
                return idValue.toString();
            }
        } catch (Exception ignored) {
        }

        PhasePulse.LOGGER.debug("Could not resolve advancement id from {}", advancement.getClass().getName());
        return null;
    }
}
