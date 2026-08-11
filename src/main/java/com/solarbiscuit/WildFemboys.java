package com.solarbiscuit;

import com.solarbiscuit.entity.FemboyEntity;
import com.solarbiscuit.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.solarbiscuit.registry.ModFluids;
import com.solarbiscuit.registry.ModItems;
import com.solarbiscuit.registry.ModMenuTypes;

@Mod(WildFemboys.MOD_ID)
public class WildFemboys {
    public static final String MOD_ID = "wildfemboys";

    @SuppressWarnings("removal")
    public WildFemboys() {
        // We get the event bus here inside the default constructor
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
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.FEMBOY.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
    }
}