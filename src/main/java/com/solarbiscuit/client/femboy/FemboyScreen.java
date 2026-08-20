package com.solarbiscuit.client.femboy;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.inventory.femboy.FemboyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FemboyScreen extends AbstractContainerScreen<FemboyMenu> {
    private static final ResourceLocation SMALL_TEXTURE = new ResourceLocation(SolarsMobs.MOD_ID, "textures/gui/femboy_inventory_small.png");
    private static final ResourceLocation LARGE_TEXTURE = new ResourceLocation(SolarsMobs.MOD_ID, "textures/gui/femboy_inventory_large.png");
    private static final ResourceLocation EMPTY_SWORD = new ResourceLocation("minecraft", "textures/item/iron_sword.png");

    public FemboyScreen(FemboyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        boolean isLarge = menu.getRowCount() == 6;

        this.imageWidth = 212; 
        this.imageHeight = isLarge ? 222 : 168; 

        this.inventoryLabelX = 59; 
        this.titleLabelX = 59;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        boolean isLarge = this.menu.getRowCount() == 6;
        ResourceLocation texture = isLarge ? LARGE_TEXTURE : SMALL_TEXTURE;
        
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        
        int drawWidth = 256; 
        
        guiGraphics.blit(texture, i, j, 0, 0, drawWidth, this.imageHeight);
        if (this.menu.slots.size() > 4 && !this.menu.slots.get(4).hasItem()) {
            guiGraphics.blit(EMPTY_SWORD, i + this.menu.slots.get(4).x, j + this.menu.slots.get(4).y, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}