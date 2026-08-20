package com.solarbiscuit.inventory.femboy;

import com.solarbiscuit.compat.curios.CuriosCompat;
import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FemboyMenu extends AbstractContainerMenu {
    private final FemboyEntity femboy;
    private final int containerRows;
    private final Container targetInv;
    private final FemboyEquipmentContainer equipWrapper;

    public FemboyMenu(int id, Inventory playerInventory, FemboyEntity entity, boolean isLarge) {
        super(ModMenuTypes.FEMBOY_MENU.get(), id);
        this.femboy = entity;
        this.containerRows = isLarge ? 6 : 3;

        Container actualInv = entity.getInventory();
        if (isLarge && actualInv.getContainerSize() < 54) {
            this.targetInv = new SimpleContainer(54);
        } else {
            this.targetInv = actualInv;
        }

        this.equipWrapper = new FemboyEquipmentContainer(femboy);

        int equipX = isLarge ? 9 : 27;
        int invX = 59;
        int armorStartY = 7; 
        int handsStartY = 90; 

        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 5, equipX, armorStartY, EquipmentSlot.HEAD));
        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 4, equipX, armorStartY + 18, EquipmentSlot.CHEST));
        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 3, equipX, armorStartY + 36, EquipmentSlot.LEGS));
        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 2, equipX, armorStartY + 54, EquipmentSlot.FEET));
        
        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 0, equipX, handsStartY, EquipmentSlot.MAINHAND));
        this.addSlot(new FemboyEquipmentContainer.GearSlot(equipWrapper, 1, equipX, handsStartY + 30, EquipmentSlot.OFFHAND));  

        for (int j = 0; j < this.containerRows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(targetInv, k + j * 9, invX + k * 18, 18 + j * 18));
            }
        }

        int yOffset = (this.containerRows - 3) * 18;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, invX + j1 * 18, 85 + l * 18 + yOffset));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, invX + i1 * 18, 143 + yOffset));
        }

        if (CuriosCompat.isLoaded()) {
            this.addSlot(CuriosCompat.createSlot(entity, CuriosCompat.BACK_SLOT, equipX, handsStartY + 52,
                    CuriosCompat.emptyIcon(CuriosCompat.BACK_SLOT)));
            this.addSlot(CuriosCompat.createSlot(entity, CuriosCompat.HEAD_SLOT, equipX, handsStartY + 70,
                    CuriosCompat.emptyIcon(CuriosCompat.HEAD_SLOT)));
        } else {
            Slot backSlot = FemboyBackpackCompat.createSlot(entity, equipX, handsStartY + 52);
            if (backSlot != null) {
                this.addSlot(backSlot);
            }
        }
    }

    public FemboyEntity getFemboy() {
        return this.femboy;
    }

    public int getRowCount() {
        return this.containerRows;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && button == 1 && clickType == ClickType.PICKUP) {
            if (FemboyBackpackCompat.handleClick(this.slots.get(slotId), player)) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.femboy.isAlive() && this.femboy.distanceTo(player) < 8.0F && this.femboy.isOwnedBy(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            int femboySlots = 6 + (this.containerRows * 9);

            if (index < femboySlots) {
                if (!this.moveItemStackTo(slotStack, femboySlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean movedToEquip = false;
                boolean lightOrShield = FemboyEquipmentContainer.isHandheldLight(slotStack)
                        || FemboyEquipmentContainer.isShield(slotStack);
                int[] order = lightOrShield
                        ? new int[]{5, 0, 1, 2, 3, 4}
                        : new int[]{0, 1, 2, 3, 4, 5};
                for (int i : order) {
                    Slot s = this.slots.get(i);
                    if (!s.hasItem() && s.mayPlace(slotStack)
                            && this.moveItemStackTo(slotStack, i, i + 1, false)) {
                        movedToEquip = true;
                        break;
                    }
                }

                if (!movedToEquip) {
                    if (!this.moveItemStackTo(slotStack, 6, femboySlots, false)) {
                        return ItemStack.EMPTY; 
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return originalStack;
    }
}