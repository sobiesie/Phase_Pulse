package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;

/**
 * Monitors player inventory/container screen time.
 * Sends an event when player spends significant time organizing inventory.
 */
public class InventoryListener {
    // Minimum time in screen to trigger "organizing" event (10 seconds)
    private static final long ORGANIZING_THRESHOLD_MS = 20000;
    // Debounce between organizing events (2 minutes)
    private static final long DEBOUNCE_MS = 120000;

    private final EventDebouncer debouncer = new EventDebouncer();

    private boolean wasInInventoryScreen = false;
    private long screenOpenedTime = 0;
    private String currentScreenType = null;

    public void onClientTick(MinecraftClient client) {
        Screen currentScreen = client.currentScreen;
        boolean isInInventoryScreen = isInventoryScreen(currentScreen);

        // Player just opened an inventory screen
        if (isInInventoryScreen && !wasInInventoryScreen) {
            screenOpenedTime = System.currentTimeMillis();
            currentScreenType = getScreenType(currentScreen);
        }

        // Player just closed an inventory screen
        if (!isInInventoryScreen && wasInInventoryScreen) {
            long timeSpent = System.currentTimeMillis() - screenOpenedTime;

            // If they spent enough time, they were probably organizing
            if (timeSpent >= ORGANIZING_THRESHOLD_MS) {
                if (debouncer.shouldTrigger("inventory_organizing", DEBOUNCE_MS)) {
                    int secondsSpent = (int) (timeSpent / 1000);

                    EventPacket packet = new EventPacket("inventory_organizing")
                            .addMetadata("screen_type", currentScreenType)
                            .addMetadata("duration_seconds", secondsSpent);

                    NetworkManager.getInstance().sendEvent(packet);
                }
            }

            // Reset tracking
            screenOpenedTime = 0;
            currentScreenType = null;
        }

        wasInInventoryScreen = isInInventoryScreen;
    }

    /**
     * Checks if the screen is an inventory/container screen.
     */
    private boolean isInventoryScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        return screen instanceof InventoryScreen
                || screen instanceof CreativeInventoryScreen
                || screen instanceof GenericContainerScreen  // Chests, barrels, etc.
                || screen instanceof ShulkerBoxScreen
                || screen instanceof Generic3x3ContainerScreen  // Dispenser, dropper
                || screen instanceof HopperScreen
                || screen instanceof FurnaceScreen
                || screen instanceof BlastFurnaceScreen
                || screen instanceof SmokerScreen
                || screen instanceof BrewingStandScreen
                || screen instanceof EnchantmentScreen
                || screen instanceof AnvilScreen
                || screen instanceof SmithingScreen
                || screen instanceof GrindstoneScreen
                || screen instanceof LoomScreen
                || screen instanceof CartographyTableScreen
                || screen instanceof StonecutterScreen
                || screen instanceof CraftingScreen
                || screen instanceof MerchantScreen
                || screen instanceof BeaconScreen;
    }

    /**
     * Gets a friendly name for the screen type.
     */
    private String getScreenType(Screen screen) {
        if (screen instanceof InventoryScreen) return "inventory";
        if (screen instanceof CreativeInventoryScreen) return "creative";
        if (screen instanceof GenericContainerScreen) return "chest";
        if (screen instanceof ShulkerBoxScreen) return "shulker_box";
        if (screen instanceof Generic3x3ContainerScreen) return "dispenser";
        if (screen instanceof HopperScreen) return "hopper";
        if (screen instanceof FurnaceScreen) return "furnace";
        if (screen instanceof BlastFurnaceScreen) return "blast_furnace";
        if (screen instanceof SmokerScreen) return "smoker";
        if (screen instanceof BrewingStandScreen) return "brewing_stand";
        if (screen instanceof EnchantmentScreen) return "enchanting_table";
        if (screen instanceof AnvilScreen) return "anvil";
        if (screen instanceof SmithingScreen) return "smithing_table";
        if (screen instanceof GrindstoneScreen) return "grindstone";
        if (screen instanceof LoomScreen) return "loom";
        if (screen instanceof CartographyTableScreen) return "cartography_table";
        if (screen instanceof StonecutterScreen) return "stonecutter";
        if (screen instanceof CraftingScreen) return "crafting_table";
        if (screen instanceof MerchantScreen) return "villager";
        if (screen instanceof BeaconScreen) return "beacon";
        return "unknown";
    }
}
