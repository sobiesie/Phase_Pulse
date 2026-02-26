package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Monitors player inventory for rare item pickups.
 * Sends events when player acquires notable rare items.
 */
public class RareItemListener {
    private static final int SCAN_INTERVAL = 10; // Check every 0.5 seconds

    private final EventDebouncer debouncer = new EventDebouncer();
    private final Map<String, Integer> lastItemCounts = new HashMap<>();
    private int tickCounter = 0;
    private boolean initialized = false;

    // Rare items to track with their categories (namespace:path keys)
    private static final Map<String, String> RARE_ITEMS = Map.ofEntries(
            // Music discs
            Map.entry("music_disc_13", "music_disc"),
            Map.entry("music_disc_cat", "music_disc"),
            Map.entry("music_disc_blocks", "music_disc"),
            Map.entry("music_disc_chirp", "music_disc"),
            Map.entry("music_disc_far", "music_disc"),
            Map.entry("music_disc_mall", "music_disc"),
            Map.entry("music_disc_mellohi", "music_disc"),
            Map.entry("music_disc_stal", "music_disc"),
            Map.entry("music_disc_strad", "music_disc"),
            Map.entry("music_disc_ward", "music_disc"),
            Map.entry("music_disc_11", "music_disc"),
            Map.entry("music_disc_wait", "music_disc"),
            Map.entry("music_disc_otherside", "music_disc"),
            Map.entry("music_disc_5", "music_disc"),
            Map.entry("music_disc_pigstep", "music_disc"),
            Map.entry("music_disc_relic", "music_disc"),
            Map.entry("music_disc_creator", "music_disc"),
            Map.entry("music_disc_creator_music_box", "music_disc"),
            Map.entry("music_disc_precipice", "music_disc"),

            // Combat/rare items
            Map.entry("totem_of_undying", "totem_of_undying"),
            Map.entry("trident", "trident"),
            Map.entry("enchanted_book", "enchanted_book"),
            Map.entry("elytra", "elytra"),
            Map.entry("dragon_egg", "dragon_egg"),
            Map.entry("nether_star", "nether_star"),
            Map.entry("heart_of_the_sea", "heart_of_the_sea"),
            Map.entry("enchanted_golden_apple", "enchanted_golden_apple"),
            Map.entry("dragon_head", "dragon_head"),
            Map.entry("wither_skeleton_skull", "wither_skeleton_skull"),
            Map.entry("beacon", "beacon"),
            Map.entry("conduit", "conduit"),

            // Sniffer items
            Map.entry("sniffer_egg", "sniffer_egg"),
            Map.entry("pitcher_pod", "pitcher_pod"),
            Map.entry("torchflower_seeds", "torchflower_seeds"),

            // Treasure
            Map.entry("diamond", "diamond"),
            Map.entry("ancient_debris", "ancient_debris"),
            Map.entry("netherite_ingot", "netherite_ingot"),
            Map.entry("netherite_scrap", "netherite_scrap"),
            Map.entry("emerald", "emerald"),

            // Special armor/tools (netherite)
            Map.entry("netherite_sword", "netherite_gear"),
            Map.entry("netherite_pickaxe", "netherite_gear"),
            Map.entry("netherite_axe", "netherite_gear"),
            Map.entry("netherite_shovel", "netherite_gear"),
            Map.entry("netherite_hoe", "netherite_gear"),
            Map.entry("netherite_helmet", "netherite_gear"),
            Map.entry("netherite_chestplate", "netherite_gear"),
            Map.entry("netherite_leggings", "netherite_gear"),
            Map.entry("netherite_boots", "netherite_gear"),

            // Mob drops
            Map.entry("shulker_shell", "shulker_shell"),
            Map.entry("phantom_membrane", "phantom_membrane"),
            Map.entry("nautilus_shell", "nautilus_shell"),
            Map.entry("turtle_scute", "turtle_scute"),
            Map.entry("rabbit_foot", "rabbit_foot"),

            // Special blocks
            Map.entry("sponge", "sponge"),
            Map.entry("wet_sponge", "sponge")
    );

    // Very rare items that deserve extra excitement
    private static final Set<String> VERY_RARE = Set.of(
            "totem_of_undying", "trident", "elytra", "dragon_egg",
            "nether_star", "enchanted_golden_apple", "netherite_ingot",
            "heart_of_the_sea", "beacon", "dragon_head"
    );

    // Milestone items that trigger the item_obtained event for progression tracking
    private static final Set<String> MILESTONE_ITEMS = Set.of(
            "elytra",
            "nether_star",
            "dragon_egg",
            "beacon",
            "totem_of_undying"
    );

    public void onClientTick(Minecraft client) {
        if (client.player == null) {
            return;
        }

        // Only scan every SCAN_INTERVAL ticks
        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Build current inventory counts for rare items
        Map<String, Integer> currentCounts = new HashMap<>();

        // Scan main inventory
        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemId == null || !itemId.getNamespace().equals("minecraft")) {
                    continue;
                }
                String itemKey = itemId.getPath();
                String category = RARE_ITEMS.get(itemKey);
                if (category != null) {
                    currentCounts.merge(itemKey, stack.getCount(), Integer::sum);
                }
            }
        }

        // On first tick, just initialize counts without sending events
        if (!initialized) {
            lastItemCounts.putAll(currentCounts);
            initialized = true;
            return;
        }

        // Check for new items
        for (Map.Entry<String, Integer> entry : currentCounts.entrySet()) {
            String itemId = entry.getKey();
            int currentCount = entry.getValue();
            int lastCount = lastItemCounts.getOrDefault(itemId, 0);

            // Player gained this item
            if (currentCount > lastCount) {
                int gained = currentCount - lastCount;
                String category = RARE_ITEMS.get(itemId);
                String simpleName = itemId;

                if (category != null) {
                    // Debounce per item type (30 second cooldown)
                    if (debouncer.shouldTrigger("rare_item_" + itemId, 30000)) {
                        boolean isVeryRare = VERY_RARE.contains(category);

                        EventPacket packet = new EventPacket("rare_item_found")
                                .addMetadata("item", simpleName)
                                .addMetadata("category", category)
                                .addMetadata("count", gained)
                                .addMetadata("is_very_rare", isVeryRare);

                        NetworkManager.getInstance().sendEvent(packet);
                    }

                    // Also emit item_obtained for milestone items (longer cooldown)
                    if (MILESTONE_ITEMS.contains(itemId)) {
                        if (debouncer.shouldTrigger("milestone_" + itemId, 60000)) {
                            EventPacket milestonePacket = new EventPacket("item_obtained")
                                    .addMetadata("item", simpleName);

                            NetworkManager.getInstance().sendEvent(milestonePacket);
                        }
                    }
                }
            }
        }

        // Update last counts
        lastItemCounts.clear();
        lastItemCounts.putAll(currentCounts);
    }

    /**
     * Resets the listener state (e.g., when player changes worlds).
     */
    public void reset() {
        lastItemCounts.clear();
        initialized = false;
    }
}
