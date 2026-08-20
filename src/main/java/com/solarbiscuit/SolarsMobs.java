package com.solarbiscuit;

import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackCompat;
import com.solarbiscuit.compat.sophisticatedstorage.FemboyStorageCompat;
import com.solarbiscuit.registry.ModBiomeModifiers;
import com.solarbiscuit.registry.MobCatalog;
import com.solarbiscuit.registry.MobEntry;
import com.solarbiscuit.registry.ModCreativeTabs;
import com.solarbiscuit.registry.ModEntities;
import com.solarbiscuit.registry.ModFluids;
import com.solarbiscuit.registry.ModItems;
import com.solarbiscuit.registry.ModMenuTypes;
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
        ModCreativeTabs.TABS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBiomeModifiers.SERIALIZERS.register(modEventBus);
        FemboyStorageCompat.register(modEventBus);
        FemboyBackpackCompat.register(modEventBus);

        modEventBus.addListener(ModEntities::addCreative);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (MobEntry<?> mob : MobCatalog.ALL) {
            mob.registerAttributes(event);
        }
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        for (MobEntry<?> mob : MobCatalog.ALL) {
            mob.registerSpawn(event);
        }
    }
}
