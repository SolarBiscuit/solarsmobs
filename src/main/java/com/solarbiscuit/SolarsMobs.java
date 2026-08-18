package com.solarbiscuit;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import com.solarbiscuit.registry.ModEntities;
import com.solarbiscuit.registry.ModFluids;
import com.solarbiscuit.registry.ModItems;
import com.solarbiscuit.registry.ModMenuTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SolarsMobs.MOD_ID)
public class SolarsMobs {
    public static final String MOD_ID = "solarsmobs";

    @SuppressWarnings("removal")
    public SolarsMobs() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(ModEntities::addCreative);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FEMBOY.get(), FemboyEntity.createAttributes().build());
        event.put(ModEntities.THIEF.get(), ThiefEntity.createAttributes().build());
        event.put(ModEntities.TEMPLAR.get(), TemplarEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.FEMBOY.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.THIEF.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.TEMPLAR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
    }
}
