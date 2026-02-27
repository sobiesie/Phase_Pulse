package com.phasepal.phasepulse.event.player;

import com.phasepal.phasepulse.event.EventDebouncer;
import com.phasepal.phasepulse.network.EventPacket;
import com.phasepal.phasepulse.network.NetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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

    // Rare items to track with their categories
    private static final Map<Item, String> RARE_ITEMS = Map.ofEntries(
            // Music discs
            Map.entry(Items.MUSIC_DISC_13, "music_disc"),
            Map.entry(Items.MUSIC_DISC_CAT, "music_disc"),
            Map.entry(Items.MUSIC_DISC_BLOCKS, "music_disc"),
            Map.entry(Items.MUSIC_DISC_CHIRP, "music_disc"),
            Map.entry(Items.MUSIC_DISC_FAR, "music_disc"),
            Map.entry(Items.MUSIC_DISC_MALL, "music_disc"),
            Map.entry(Items.MUSIC_DISC_MELLOHI, "music_disc"),
            Map.entry(Items.MUSIC_DISC_STAL, "music_disc"),
            Map.entry(Items.MUSIC_DISC_STRAD, "music_disc"),
            Map.entry(Items.MUSIC_DISC_WARD, "music_disc"),
            Map.entry(Items.MUSIC_DISC_11, "music_disc"),
            Map.entry(Items.MUSIC_DISC_WAIT, "music_disc"),
            Map.entry(Items.MUSIC_DISC_OTHERSIDE, "music_disc"),
            Map.entry(Items.MUSIC_DISC_5, "music_disc"),
            Map.entry(Items.MUSIC_DISC_PIGSTEP, "music_disc"),
            Map.entry(Items.MUSIC_DISC_RELIC, "music_disc"),

            // Combat/rare items
            Map.entry(Items.TOTEM_OF_UNDYING, "totem_of_undying"),
            Map.entry(Items.TRIDENT, "trident"),
            Map.entry(Items.ENCHANTED_BOOK, "enchanted_book"),
            Map.entry(Items.ELYTRA, "elytra"),
            Map.entry(Items.DRAGON_EGG, "dragon_egg"),
            Map.entry(Items.NETHER_STAR, "nether_star"),
            Map.entry(Items.HEART_OF_THE_SEA, "heart_of_the_sea"),
            Map.entry(Items.ENCHANTED_GOLDEN_APPLE, "enchanted_golden_apple"),
            Map.entry(Items.DRAGON_HEAD, "dragon_head"),
            Map.entry(Items.WITHER_SKELETON_SKULL, "wither_skeleton_skull"),
            Map.entry(Items.BEACON, "beacon"),
            Map.entry(Items.CONDUIT, "conduit"),

            // Sniffer items
            Map.entry(Items.SNIFFER_EGG, "sniffer_egg"),
            Map.entry(Items.PITCHER_POD, "pitcher_pod"),
            Map.entry(Items.TORCHFLOWER_SEEDS, "torchflower_seeds"),

            // Treasure
            Map.entry(Items.DIAMOND, "diamond"),
            Map.entry(Items.ANCIENT_DEBRIS, "ancient_debris"),
            Map.entry(Items.NETHERITE_INGOT, "netherite_ingot"),
            Map.entry(Items.NETHERITE_SCRAP, "netherite_scrap"),
            Map.entry(Items.EMERALD, "emerald"),

            // Special armor/tools (netherite)
            Map.entry(Items.NETHERITE_SWORD, "netherite_gear"),
            Map.entry(Items.NETHERITE_PICKAXE, "netherite_gear"),
            Map.entry(Items.NETHERITE_AXE, "netherite_gear"),
            Map.entry(Items.NETHERITE_SHOVEL, "netherite_gear"),
            Map.entry(Items.NETHERITE_HOE, "netherite_gear"),
            Map.entry(Items.NETHERITE_HELMET, "netherite_gear"),
            Map.entry(Items.NETHERITE_CHESTPLATE, "netherite_gear"),
            Map.entry(Items.NETHERITE_LEGGINGS, "netherite_gear"),
            Map.entry(Items.NETHERITE_BOOTS, "netherite_gear"),

            // Mob drops
            Map.entry(Items.SHULKER_SHELL, "shulker_shell"),
            Map.entry(Items.PHANTOM_MEMBRANE, "phantom_membrane"),
            Map.entry(Items.NAUTILUS_SHELL, "nautilus_shell"),
            Map.entry(Items.TURTLE_SCUTE, "turtle_scute"),
            Map.entry(Items.RABBIT_FOOT, "rabbit_foot"),

            // Special blocks
            Map.entry(Items.SPONGE, "sponge"),
            Map.entry(Items.WET_SPONGE, "sponge")
    );

    // Very rare items that deserve extra excitement
    private static final Set<String> VERY_RARE = Set.of(
            "totem_of_undying", "trident", "elytra", "dragon_egg",
            "nether_star", "enchanted_golden_apple", "netherite_ingot",
            "heart_of_the_sea", "beacon", "dragon_head"
    );

    // Milestone items that trigger the item_obtained event for progression tracking
    private static final Set<Item> MILESTONE_ITEMS = Set.of(
            Items.ELYTRA,
            Items.NETHER_STAR,
            Items.DRAGON_EGG,
            Items.BEACON,
            Items.TOTEM_OF_UNDYING
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
            if (!stack.isEmpty()) {
                String category = RARE_ITEMS.get(stack.getItem());
                if (category != null) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    currentCounts.merge(itemId, stack.getCount(), Integer::sum);
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
                Identifier identifier = parseIdentifier(itemId);
                Item item = Registries.ITEM.get(identifier);
                String category = RARE_ITEMS.get(item);

                if (category != null) {
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
                    if (MILESTONE_ITEMS.contains(item)) {
                        if (debouncer.shouldTrigger("milestone_" + itemId, 60000)) {
                            String simpleName = itemId.replace("minecraft:", "");
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

    private Identifier parseIdentifier(String itemId) {
        int separator = itemId.indexOf(':');
        if (separator > 0 && separator < itemId.length() - 1) {
            return Identifier.of(itemId.substring(0, separator), itemId.substring(separator + 1));
        }
        return Identifier.of("minecraft", itemId);
    }
}
