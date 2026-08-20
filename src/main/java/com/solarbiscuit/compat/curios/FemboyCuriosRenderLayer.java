package com.solarbiscuit.compat.curios;



import com.mojang.blaze3d.vertex.PoseStack;

import com.solarbiscuit.client.femboy.FemboyModel;

import com.solarbiscuit.compat.simplehats.SimpleHatsCompat;

import com.solarbiscuit.entity.femboy.FemboyEntity;

import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.entity.RenderLayerParent;

import net.minecraft.client.renderer.entity.layers.RenderLayer;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

import top.theillusivec4.curios.api.CuriosApi;

import top.theillusivec4.curios.api.SlotContext;

import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import top.theillusivec4.curios.api.client.ICurioRenderer;

import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;



import java.util.List;

import java.util.Map;

import java.util.Optional;



public class FemboyCuriosRenderLayer extends RenderLayer<FemboyEntity, FemboyModel> {

    private final RenderLayerParent<FemboyEntity, FemboyModel> parent;



    public FemboyCuriosRenderLayer(RenderLayerParent<FemboyEntity, FemboyModel> renderer) {

        super(renderer);

        this.parent = renderer;

    }



    @Override

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, FemboyEntity femboy,

                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,

                       float netHeadYaw, float headPitch) {

        CuriosApi.getCuriosInventory(femboy).ifPresent(handler -> {

            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {

                ICurioStacksHandler stacksHandler = entry.getValue();

                IDynamicStackHandler stacks = stacksHandler.getStacks();

                IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();

                List<Boolean> renders = stacksHandler.getRenders();

                for (int i = 0; i < stacks.getSlots(); i++) {

                    if (i < renders.size() && !renders.get(i)) {

                        continue;

                    }

                    ItemStack stack = i < cosmetics.getSlots() ? cosmetics.getStackInSlot(i) : ItemStack.EMPTY;

                    if (stack.isEmpty()) {

                        stack = stacks.getStackInSlot(i);

                    }

                    if (stack.isEmpty()) {

                        continue;

                    }

                    renderCurio(poseStack, buffer, packedLight, femboy, limbSwing, limbSwingAmount, partialTicks,

                            ageInTicks, netHeadYaw, headPitch, entry.getKey(), i, stack);

                }

            }

        });

    }



    private void renderCurio(PoseStack poseStack, MultiBufferSource buffer, int packedLight, FemboyEntity femboy,

                             float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,

                             float netHeadYaw, float headPitch, String slotId, int index, ItemStack stack) {

        if (SimpleHatsCompat.isLoaded() && isSimpleHats(stack)

                && (CuriosCompat.HEAD_SLOT.equals(slotId) || "hat".equals(slotId))) {

            return;

        }

        Optional<ICurioRenderer> renderer = CuriosRendererRegistry.getRenderer(stack.getItem());

        if (renderer.isPresent()) {

            renderer.get().render(stack, new SlotContext(slotId, femboy, index, false, true),

                    poseStack, this.parent, buffer, packedLight, limbSwing, limbSwingAmount,

                    partialTicks, ageInTicks, netHeadYaw, headPitch);

        }

    }



    private static boolean isSimpleHats(ItemStack stack) {

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        return id != null && SimpleHatsCompat.MOD_ID.equals(id.getNamespace());

    }

}

