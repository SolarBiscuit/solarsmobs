package com.solarbiscuit.client.arborist;

import com.solarbiscuit.client.skin.HumanoidSkinTextures;
import com.solarbiscuit.entity.arborist.ArboristEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class ArboristRenderer extends HumanoidMobRenderer<ArboristEntity, HumanoidModel<ArboristEntity>> {
    private static final HumanoidSkinTextures SKINS = new HumanoidSkinTextures("arborist");

    public ArboristRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ArboristEntity entity) {
        return SKINS.pick(0);
    }
}
