package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

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

    // Rare items to track by string id, so missing 1.21 fields don't break 1.20.4 compilation
    private static final Map<String, String> RARE_ITEMS = Map.ofEntries(
            // Music discs
            Map.entry("minecraft:music_disc_13", "music_disc"),
            Map.entry("minecraft:music_disc_cat", "music_disc"),
            Map.entry("minecraft:music_disc_blocks", "music_disc"),
            Map.entry("minecraft:music_disc_chirp", "music_disc"),
            Map.entry("minecraft:music_disc_far", "music_disc"),
            Map.entry("minecraft:music_disc_mall", "music_disc"),
            Map.entry("minecraft:music_disc_mellohi", "music_disc"),
            Map.entry("minecraft:music_disc_stal", "music_disc"),
            Map.entry("minecraft:music_disc_strad", "music_disc"),
            Map.entry("minecraft:music_disc_ward", "music_disc"),
            Map.entry("minecraft:music_disc_11", "music_disc"),
            Map.entry("minecraft:music_disc_wait", "music_disc"),
            Map.entry("minecraft:music_disc_otherside", "music_disc"),
            Map.entry("minecraft:music_disc_5", "music_disc"),
            Map.entry("minecraft:music_disc_pigstep", "music_disc"),
            Map.entry("minecraft:music_disc_relic", "music_disc"),
            Map.entry("minecraft:music_disc_creator", "music_disc"),
            Map.entry("minecraft:music_disc_creator_music_box", "music_disc"),
            Map.entry("minecraft:music_disc_precipice", "music_disc"),

            // Combat/rare items
            Map.entry("minecraft:totem_of_undying", "totem_of_undying"),
            Map.entry("minecraft:trident", "trident"),
            Map.entry("minecraft:enchanted_book", "enchanted_book"),
            Map.entry("minecraft:elytra", "elytra"),
            Map.entry("minecraft:dragon_egg", "dragon_egg"),
            Map.entry("minecraft:nether_star", "nether_star"),
            Map.entry("minecraft:heart_of_the_sea", "heart_of_the_sea"),
            Map.entry("minecraft:enchanted_golden_apple", "enchanted_golden_apple"),
            Map.entry("minecraft:dragon_head", "dragon_head"),
            Map.entry("minecraft:wither_skeleton_skull", "wither_skeleton_skull"),
            Map.entry("minecraft:beacon", "beacon"),
            Map.entry("minecraft:conduit", "conduit"),

            // Sniffer items
            Map.entry("minecraft:sniffer_egg", "sniffer_egg"),
            Map.entry("minecraft:pitcher_pod", "pitcher_pod"),
            Map.entry("minecraft:torchflower_seeds", "torchflower_seeds"),

            // Treasure
            Map.entry("minecraft:diamond", "diamond"),
            Map.entry("minecraft:ancient_debris", "ancient_debris"),
            Map.entry("minecraft:netherite_ingot", "netherite_ingot"),
            Map.entry("minecraft:netherite_scrap", "netherite_scrap"),
            Map.entry("minecraft:emerald", "emerald"),

            // Special armor/tools (netherite)
            Map.entry("minecraft:netherite_sword", "netherite_gear"),
            Map.entry("minecraft:netherite_pickaxe", "netherite_gear"),
            Map.entry("minecraft:netherite_axe", "netherite_gear"),
            Map.entry("minecraft:netherite_shovel", "netherite_gear"),
            Map.entry("minecraft:netherite_hoe", "netherite_gear"),
            Map.entry("minecraft:netherite_helmet", "netherite_gear"),
            Map.entry("minecraft:netherite_chestplate", "netherite_gear"),
            Map.entry("minecraft:netherite_leggings", "netherite_gear"),
            Map.entry("minecraft:netherite_boots", "netherite_gear"),

            // Mob drops
            Map.entry("minecraft:shulker_shell", "shulker_shell"),
            Map.entry("minecraft:phantom_membrane", "phantom_membrane"),
            Map.entry("minecraft:nautilus_shell", "nautilus_shell"),
            Map.entry("minecraft:turtle_scute", "turtle_scute"),
            Map.entry("minecraft:rabbit_foot", "rabbit_foot"),

            // Special blocks
            Map.entry("minecraft:sponge", "sponge"),
            Map.entry("minecraft:wet_sponge", "sponge")
    );

    // Very rare items that deserve extra excitement
    private static final Set<String> VERY_RARE = Set.of(
            "totem_of_undying", "trident", "elytra", "dragon_egg",
            "nether_star", "enchanted_golden_apple", "netherite_ingot",
            "heart_of_the_sea", "beacon", "dragon_head"
    );

    // Milestone items that trigger the item_obtained event for progression tracking
    private static final Set<String> MILESTONE_ITEMS = Set.of(
            "minecraft:elytra",
            "minecraft:nether_star",
            "minecraft:dragon_egg",
            "minecraft:beacon",
            "minecraft:totem_of_undying"
    );

    public void onClientTick(MinecraftClient client) {
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
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            String itemId = Registry.ITEM.getId(stack.getItem()).toString();
            if (RARE_ITEMS.containsKey(itemId)) {
                currentCounts.merge(itemId, stack.getCount(), Integer::sum);
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
                if (category == null) {
                    continue;
                }

                // Debounce per item type (30 second cooldown)
                if (debouncer.shouldTrigger("rare_item_" + itemId, 30000)) {
                    boolean isVeryRare = VERY_RARE.contains(category);
                    String simpleName = itemId.replace("minecraft:", "");

                    EventPacket packet = new EventPacket("rare_item_found")
                            .addMetadata("item", simpleName)
                            .addMetadata("category", category)
                            .addMetadata("count", gained)
                            .addMetadata("is_very_rare", isVeryRare);

                    NetworkManager.getInstance().sendEvent(packet);
                }

                // Also emit item_obtained for milestone items (longer cooldown)
                if (MILESTONE_ITEMS.contains(itemId) && debouncer.shouldTrigger("milestone_" + itemId, 60000)) {
                    String simpleName = itemId.replace("minecraft:", "");
                    EventPacket milestonePacket = new EventPacket("item_obtained")
                            .addMetadata("item", simpleName);

                    NetworkManager.getInstance().sendEvent(milestonePacket);
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
