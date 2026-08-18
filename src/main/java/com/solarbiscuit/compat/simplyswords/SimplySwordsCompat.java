package com.solarbiscuit.compat.simplyswords;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public final class SimplySwordsCompat {
    public static final String MOD_ID = "simplyswords";

    private SimplySwordsCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    @SuppressWarnings("removal")
    public static ItemStack randomIronWeapon(RandomSource random) {
        if (isLoaded()) {
            ResourceLocation id = switch (random.nextInt(3)) {
                case 0 -> new ResourceLocation(MOD_ID, "iron_longsword");
                case 1 -> new ResourceLocation(MOD_ID, "iron_rapier");
                default -> new ResourceLocation("minecraft", "iron_sword");
            };
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        return new ItemStack(Items.IRON_SWORD);
    }
}
