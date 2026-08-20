package com.solarbiscuit.entity.arborist;

import com.solarbiscuit.util.SaplingItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ArboristTrades {
    private ArboristTrades() {}

    static MerchantOffers createOffers(RandomSource random) {
        List<ItemStack> costs = vanillaCosts();
        Collections.shuffle(costs, new java.util.Random(random.nextLong()));
        List<Item> saplings = new ArrayList<>(SaplingItems.all());
        Collections.shuffle(saplings, new java.util.Random(random.nextLong()));

        MerchantOffers offers = new MerchantOffers();
        int tradeCount = Math.min(20, costs.size());
        for (int i = 0; i < tradeCount; i++) {
            Item sapling = saplings.get(i % saplings.size());
            int saplingCount = 1 + random.nextInt(4);
            ItemStack cost = costs.get(i).copy();
            offers.add(new MerchantOffer(cost, new ItemStack(sapling, saplingCount), 12, 2, 0.05F));
        }
        return offers;
    }

    private static List<ItemStack> vanillaCosts() {
        List<ItemStack> costs = new ArrayList<>();
        costs.add(new ItemStack(Items.WHEAT, 20));
        costs.add(new ItemStack(Items.POTATO, 26));
        costs.add(new ItemStack(Items.CARROT, 22));
        costs.add(new ItemStack(Items.BEETROOT, 15));
        costs.add(new ItemStack(Items.PUMPKIN, 6));
        costs.add(new ItemStack(Items.MELON, 4));
        costs.add(new ItemStack(Items.COAL, 15));
        costs.add(new ItemStack(Items.RAW_IRON, 4));
        costs.add(new ItemStack(Items.RAW_GOLD, 1));
        costs.add(new ItemStack(Items.IRON_INGOT, 4));
        costs.add(new ItemStack(Items.GOLD_INGOT, 3));
        costs.add(new ItemStack(Items.STRING, 20));
        costs.add(new ItemStack(Items.LEATHER, 6));
        costs.add(new ItemStack(Items.PAPER, 24));
        costs.add(new ItemStack(Items.BOOK, 1));
        costs.add(new ItemStack(Items.STICK, 32));
        costs.add(new ItemStack(Items.FLINT, 26));
        costs.add(new ItemStack(Items.CHICKEN, 14));
        costs.add(new ItemStack(Items.PORKCHOP, 7));
        costs.add(new ItemStack(Items.EMERALD, 1));
        costs.add(new ItemStack(Items.EMERALD, 2));
        costs.add(new ItemStack(Items.EMERALD, 4));
        return costs;
    }
}
