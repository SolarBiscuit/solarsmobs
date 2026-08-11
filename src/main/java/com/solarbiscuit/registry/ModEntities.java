package com.solarbiscuit.registry;

import com.solarbiscuit.WildFemboys;
import com.solarbiscuit.entity.FemboyEntity;
import net.minecraft.resources.ResourceLocation;
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
import com.solarbiscuit.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WildFemboys.MOD_ID);
    
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, WildFemboys.MOD_ID);

    // Entity
    public static final RegistryObject<EntityType<FemboyEntity>> FEMBOY =
            ENTITY_TYPES.register("femboy", () -> EntityType.Builder.of(FemboyEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F) // Standard player/humanoid hit-box
                    .build(WildFemboys.MOD_ID + ":femboy"));
					
    // Spawn Egg
    public static final RegistryObject<Item> FEMBOY_SPAWN_EGG = ITEMS.register("femboy_spawn_egg",
            () -> new ForgeSpawnEggItem(FEMBOY, 0xFFB6C1, 0xFFFFFF, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        ITEMS.register(eventBus);
    }

public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(FEMBOY_SPAWN_EGG.get());
        }
        
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.FEMBOY_MILK_BUCKET.get());
        }
    }
}