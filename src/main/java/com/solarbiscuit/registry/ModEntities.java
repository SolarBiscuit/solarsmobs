package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SolarsMobs.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SolarsMobs.MOD_ID);

    public static final RegistryObject<EntityType<FemboyEntity>> FEMBOY =
            ENTITY_TYPES.register("femboy", () -> EntityType.Builder.of(FemboyEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":femboy"));

    public static final RegistryObject<EntityType<ThiefEntity>> THIEF =
            ENTITY_TYPES.register("thief", () -> EntityType.Builder.of(ThiefEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":thief"));

    public static final RegistryObject<Item> FEMBOY_SPAWN_EGG = ITEMS.register("femboy_spawn_egg",
            () -> new ForgeSpawnEggItem(FEMBOY, 0xFFB6C1, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> THIEF_SPAWN_EGG = ITEMS.register("thief_spawn_egg",
            () -> new ForgeSpawnEggItem(THIEF, 0x5C4033, 0x1A1A1A, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        ITEMS.register(eventBus);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(FEMBOY_SPAWN_EGG.get());
            event.accept(THIEF_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.FEMBOY_MILK_BUCKET.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.THIEVES_GUILD_NECKLACE.get());
        }
    }
}
