package com.solarbiscuit.compat.sophisticatedstorage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import com.solarbiscuit.inventory.femboy.FemboyEquipmentContainer;
import com.solarbiscuit.inventory.femboy.RightClickOpenSlot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;

import java.util.List;

public class FemboyStorageScreen extends StorageScreenBase<FemboyStorageMenu> {
    private static final ResourceLocation EMPTY_SWORD = new ResourceLocation("minecraft", "textures/item/iron_sword.png");

    public FemboyStorageScreen(FemboyStorageMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void updateExtraSlotsPositions() {
        List<Slot> extras = this.menu.getExtraSlots();
        for (int i = 0; i < extras.size(); i++) {
            Slot slot = extras.get(i);
            slot.x = FemboyStorageAccess.EXTRA_SLOT_X;
            slot.y = i < 6
                    ? FemboyStorageAccess.GEAR_SLOT_Y + i * 18
                    : FemboyStorageAccess.CURIOS_SLOT_Y + (i - 6) * 18;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        List<Slot> extras = this.menu.getExtraSlots();
        if (extras.isEmpty()) {
            return;
        }
        drawClusterBorder(graphics, extras.subList(0, Math.min(6, extras.size())));
        if (extras.size() > 6) {
            drawClusterBorder(graphics, extras.subList(6, extras.size()));
        }
        for (Slot extra : extras) {
            GuiHelper.renderSlotsBackground(graphics, this.leftPos + extra.x - 1, this.topPos + extra.y - 1, 1, 1);
            if (!extra.hasItem()
                    && extra instanceof FemboyEquipmentContainer.GearSlot gear
                    && gear.getEquipmentSlot() == EquipmentSlot.MAINHAND) {
                graphics.blit(EMPTY_SWORD, this.leftPos + extra.x, this.topPos + extra.y, 0, 0, 16, 16, 16, 16);
            }
        }
        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0.0D);
        for (Slot extra : extras) {
            this.renderSlot(graphics, extra);
            if (this.isHovering(extra, mouseX, mouseY)) {
                renderSlotHighlight(graphics, extra.x, extra.y, 0);
            }
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && this.minecraft != null && this.minecraft.gameMode != null && this.minecraft.player != null) {
            for (Slot extra : this.menu.getExtraSlots()) {
                if (this.isHovering(extra, mouseX, mouseY)
                        && extra instanceof RightClickOpenSlot openSlot
                        && openSlot.openFor(this.minecraft.player)) {
                    this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, extra.index, button,
                            ClickType.PICKUP, this.minecraft.player);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        for (Slot extra : this.menu.getExtraSlots()) {
            if (this.isHovering(extra, mouseX, mouseY) && extra.hasItem()) {
                graphics.renderTooltip(this.font, extra.getItem(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return StorageTranslationHelper.INSTANCE.translGui("settings.tooltip");
    }

    private void drawClusterBorder(GuiGraphics graphics, List<Slot> slots) {
        if (slots.isEmpty()) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Slot slot : slots) {
            minX = Math.min(minX, slot.x);
            minY = Math.min(minY, slot.y);
            maxX = Math.max(maxX, slot.x + 16);
            maxY = Math.max(maxY, slot.y + 16);
        }
        int x0 = this.leftPos + minX - 4;
        int y0 = this.topPos + minY - 4;
        int x1 = this.leftPos + maxX + 4;
        int y1 = this.topPos + maxY + 4;
        int outer = 0xFF1A1A1A;
        int inner = 0xFF8B8B8B;
        graphics.fill(x0, y0, x1, y0 + 1, outer);
        graphics.fill(x0, y1 - 1, x1, y1, outer);
        graphics.fill(x0, y0, x0 + 1, y1, outer);
        graphics.fill(x1 - 1, y0, x1, y1, outer);
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y0 + 2, inner);
        graphics.fill(x0 + 1, y1 - 2, x1 - 1, y1 - 1, inner);
        graphics.fill(x0 + 1, y0 + 1, x0 + 2, y1 - 1, inner);
        graphics.fill(x1 - 2, y0 + 1, x1 - 1, y1 - 1, inner);
    }
}
