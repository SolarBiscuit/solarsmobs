package com.solarbiscuit.client;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.client.femboy.FemboyScreen;
import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackClient;
import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackCompat;
import com.solarbiscuit.compat.sophisticatedstorage.FemboyStorageClient;
import com.solarbiscuit.compat.sophisticatedstorage.FemboyStorageCompat;
import com.solarbiscuit.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.FEMBOY_MENU.get(), FemboyScreen::new);
            if (FemboyStorageCompat.isLoaded()) {
                FemboyStorageClient.registerScreens();
            }
            if (FemboyBackpackCompat.isLoaded()) {
                FemboyBackpackClient.registerScreens();
            }
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientMobCatalog.registerRenderers(event);
    }
}
