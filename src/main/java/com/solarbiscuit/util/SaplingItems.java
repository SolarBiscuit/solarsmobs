package com.solarbiscuit.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Any registered item whose id path contains {@code sapling}, including modded saplings.
 */
public final class SaplingItems {
    private SaplingItems() {}

    public static List<Item> all() {
        List<Item> saplings = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && id.getPath().contains("sapling") && item != Items.AIR) {
                saplings.add(item);
            }
        }
        if (saplings.isEmpty()) {
            saplings.add(Items.OAK_SAPLING);
            saplings.add(Items.BIRCH_SAPLING);
            saplings.add(Items.SPRUCE_SAPLING);
            saplings.add(Items.JUNGLE_SAPLING);
            saplings.add(Items.ACACIA_SAPLING);
            saplings.add(Items.DARK_OAK_SAPLING);
            saplings.add(Items.CHERRY_SAPLING);
            saplings.add(Items.MANGROVE_PROPAGULE);
        }
        return saplings;
    }

    public static ItemStack randomStack(int count) {
        List<Item> saplings = all();
        Item item = saplings.get(ThreadLocalRandom.current().nextInt(saplings.size()));
        return new ItemStack(item, Math.max(1, count));
    }
}
