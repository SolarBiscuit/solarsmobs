package com.solarbiscuit.client.endwarrior;

import com.solarbiscuit.client.skin.HumanoidSkinTextures;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class EndWarriorRenderer extends HumanoidMobRenderer<EndWarriorEntity, HumanoidModel<EndWarriorEntity>> {
    private static final HumanoidSkinTextures SKINS = new HumanoidSkinTextures("end_warrior");

    public EndWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(EndWarriorEntity entity) {
        return SKINS.pick(0);
    }
}
