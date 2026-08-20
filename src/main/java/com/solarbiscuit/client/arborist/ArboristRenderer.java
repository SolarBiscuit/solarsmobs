package com.solarbiscuit.client.arborist;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.arborist.ArboristEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class ArboristRenderer extends HumanoidMobRenderer<ArboristEntity, HumanoidModel<ArboristEntity>> {
    @SuppressWarnings("removal")
    private static final ResourceLocation DEFAULT_SKIN =
            new ResourceLocation(SolarsMobs.MOD_ID, "textures/entity/arborist/arborist.png");
    @SuppressWarnings("removal")
    private static final ResourceLocation AETHER_SKIN =
            new ResourceLocation(SolarsMobs.MOD_ID, "textures/entity/arborist/arborist_aether.png");

    public ArboristRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ArboristEntity entity) {
        if (isInAether(entity) && hasAetherSkin()) {
            return AETHER_SKIN;
        }
        return DEFAULT_SKIN;
    }

    private static boolean isInAether(ArboristEntity entity) {
        return "aether".equals(entity.level().dimension().location().getNamespace());
    }

    private static boolean hasAetherSkin() {
        return Minecraft.getInstance().getResourceManager().getResource(AETHER_SKIN).isPresent();
    }
}
