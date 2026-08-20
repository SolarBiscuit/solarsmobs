package com.solarbiscuit.compat.jade;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class SolarsMobsJadePlugin implements IWailaPlugin {
    @SuppressWarnings("removal")
    public static final ResourceLocation FEMBOY_CHEST = new ResourceLocation(SolarsMobs.MOD_ID, "femboy_chest");
    @SuppressWarnings("removal")
    public static final ResourceLocation TEMPLAR_HIRE = new ResourceLocation(SolarsMobs.MOD_ID, "templar_hire");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(FemboyChestProvider.INSTANCE, FemboyEntity.class);
        registration.registerEntityDataProvider(TemplarHireProvider.INSTANCE, TemplarEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(FemboyChestProvider.INSTANCE, FemboyEntity.class);
        registration.registerEntityComponent(TemplarHireProvider.INSTANCE, TemplarEntity.class);
    }
}
