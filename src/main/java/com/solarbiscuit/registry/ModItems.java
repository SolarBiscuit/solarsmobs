package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.item.HolyCrossItem;
import com.solarbiscuit.item.SacredEnderRingItem;
import com.solarbiscuit.item.ThievesGuildNecklaceItem;
import com.solarbiscuit.item.femboy.FemboyMilkBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SolarsMobs.MOD_ID);

    public static final RegistryObject<Item> FEMBOY_SPAWN_EGG = ITEMS.register("femboy_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FEMBOY, 0xFFB6C1, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> THIEF_SPAWN_EGG = ITEMS.register("thief_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THIEF, 0x5C4033, 0x1A1A1A, new Item.Properties()));

    public static final RegistryObject<Item> TEMPLAR_SPAWN_EGG = ITEMS.register("templar_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TEMPLAR, 0xF0F0F0, 0xB22222, new Item.Properties()));

    public static final RegistryObject<Item> END_WARRIOR_SPAWN_EGG = ITEMS.register("end_warrior_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.END_WARRIOR, 0x1B0A2A, 0xC084FC, new Item.Properties()));

    public static final RegistryObject<Item> ARCHER_SPAWN_EGG = ITEMS.register("archer_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ARCHER, 0x3D5A2A, 0xC4A574, new Item.Properties()));

    public static final RegistryObject<Item> ARBORIST_SPAWN_EGG = ITEMS.register("arborist_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ARBORIST, 0x2E7D32, 0x8D6E63, new Item.Properties()));

    public static final RegistryObject<Item> FEMBOY_MILK_BUCKET = ITEMS.register("femboy_milk_bucket",
            () -> new FemboyMilkBucketItem(new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<Item> THIEVES_GUILD_NECKLACE = ITEMS.register("thieves_guild_necklace",
            () -> new ThievesGuildNecklaceItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> HOLY_CROSS = ITEMS.register("holy_cross",
            () -> new HolyCrossItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SACRED_ENDER_RING = ITEMS.register("sacred_ender_ring",
            () -> new SacredEnderRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
}
