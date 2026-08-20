package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.archer.ArcherEntity;
import com.solarbiscuit.entity.arborist.ArboristEntity;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SolarsMobs.MOD_ID);

    public static final RegistryObject<EntityType<FemboyEntity>> FEMBOY =
            ENTITY_TYPES.register("femboy", () -> EntityType.Builder.of(FemboyEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":femboy"));

    public static final RegistryObject<EntityType<ThiefEntity>> THIEF =
            ENTITY_TYPES.register("thief", () -> EntityType.Builder.of(ThiefEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":thief"));

    public static final RegistryObject<EntityType<TemplarEntity>> TEMPLAR =
            ENTITY_TYPES.register("templar", () -> EntityType.Builder.of(TemplarEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":templar"));

    public static final RegistryObject<EntityType<EndWarriorEntity>> END_WARRIOR =
            ENTITY_TYPES.register("end_warrior", () -> EntityType.Builder.of(EndWarriorEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":end_warrior"));

    public static final RegistryObject<EntityType<ArcherEntity>> ARCHER =
            ENTITY_TYPES.register("archer", () -> EntityType.Builder.of(ArcherEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":archer"));

    public static final RegistryObject<EntityType<ArboristEntity>> ARBORIST =
            ENTITY_TYPES.register("arborist", () -> EntityType.Builder.of(ArboristEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(SolarsMobs.MOD_ID + ":arborist"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (MobEntry<?> mob : MobCatalog.ALL) {
                event.accept(mob.spawnEgg().get());
            }
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.FEMBOY_MILK_BUCKET.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.THIEVES_GUILD_NECKLACE.get());
            event.accept(ModItems.HOLY_CROSS.get());
            event.accept(ModItems.SACRED_ENDER_RING.get());
        }
    }
}
