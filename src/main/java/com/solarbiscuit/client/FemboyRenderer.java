package com.solarbiscuit.client;

import com.solarbiscuit.entity.FemboyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class FemboyRenderer extends HumanoidMobRenderer<FemboyEntity, HumanoidModel<FemboyEntity>> {
    
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;

    public FemboyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this, 
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(FemboyEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // THE MAGIC BULLET: Forces the visual model to bend into a crouch without touching the actual hitbox!
        this.model.crouching = entity.isOrderedToSit();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    @SuppressWarnings("removal") 
    public ResourceLocation getTextureLocation(FemboyEntity entity) {
        if (!texturesLoaded) {
            loadTextures();
        }
        if (TEXTURES.isEmpty()) {
            return new ResourceLocation("minecraft:textures/entity/zombie/zombie.png");
        }
        int skinIndex = Math.abs(entity.getUUID().hashCode()) % TEXTURES.size();
        return TEXTURES.get(skinIndex);
    }

    private void loadTextures() {
        Map<ResourceLocation, ?> resources = Minecraft.getInstance().getResourceManager()
                .listResources("textures/entity/femboy", resourceLocation -> resourceLocation.getPath().endsWith(".png"));

        for (ResourceLocation location : resources.keySet()) {
            TEXTURES.add(location);
        }
        texturesLoaded = true;
    }
}