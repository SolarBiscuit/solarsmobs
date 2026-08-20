package com.solarbiscuit.compat.simplehats;

import com.solarbiscuit.client.femboy.FemboyModel;
import com.solarbiscuit.client.femboy.FemboyRenderer;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.fml.ModList;

public final class SimpleHatsCompat {
    public static final String MOD_ID = "simplehats";

    private SimpleHatsCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    @SuppressWarnings("unchecked")
    public static void addHatLayer(FemboyRenderer renderer) {
        if (!isLoaded()) {
            return;
        }
        try {
            Class<?> hatLayerClass = Class.forName("fonnymunkey.simplehats.client.hat.HatLayer");
            RenderLayer<FemboyEntity, FemboyModel> layer = (RenderLayer<FemboyEntity, FemboyModel>) hatLayerClass
                    .getConstructor(RenderLayerParent.class)
                    .newInstance(renderer);
            renderer.addLayer(layer);
        } catch (Throwable ignored) {
        }
    }
}
