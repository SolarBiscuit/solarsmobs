package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.item.HolyCrossItem;
import com.solarbiscuit.item.SacredEnderRingItem;
import com.solarbiscuit.item.ThievesGuildNecklaceItem;
import com.solarbiscuit.item.femboy.FemboyMilkBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SolarsMobs.MOD_ID);

    public static final RegistryObject<Item> FEMBOY_MILK_BUCKET = ITEMS.register("femboy_milk_bucket",
            () -> new FemboyMilkBucketItem(new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<Item> THIEVES_GUILD_NECKLACE = ITEMS.register("thieves_guild_necklace",
            () -> new ThievesGuildNecklaceItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> HOLY_CROSS = ITEMS.register("holy_cross",
            () -> new HolyCrossItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SACRED_ENDER_RING = ITEMS.register("sacred_ender_ring",
            () -> new SacredEnderRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
}
