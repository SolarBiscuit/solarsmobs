package com.solarbiscuit.wildfemboys.inventory;

import com.solarbiscuit.entity.FemboyEntity;
import com.solarbiscuit.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;

public class FemboyMenu extends AbstractContainerMenu {
    private final FemboyEntity femboy;
    private final int containerRows;
    private final Container targetInv;
    private final Container equipWrapper;

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

        this.equipWrapper = new SimpleContainer(6) {
            @Override
            public void setItem(int slot, ItemStack stack) {
                EquipmentSlot type = EquipmentSlot.values()[slot];
                femboy.setItemSlot(type, stack);
            }
            @Override
            public ItemStack getItem(int slot) {
                return femboy.getItemBySlot(EquipmentSlot.values()[slot]);
            }
            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack stack = getItem(slot);
                if (!stack.isEmpty()) {
                    setItem(slot, ItemStack.EMPTY);
                }
                return stack;
            }
            @Override
            public void setChanged() { }
        };

        int equipX = isLarge ? 9 : 27;
        int invX = 59;
        int armorStartY = 7; 
        int handsStartY = 90; 

        this.addSlot(new Slot(equipWrapper, 5, equipX, armorStartY));       
        this.addSlot(new Slot(equipWrapper, 4, equipX, armorStartY + 18));  
        this.addSlot(new Slot(equipWrapper, 3, equipX, armorStartY + 36));  
        this.addSlot(new Slot(equipWrapper, 2, equipX, armorStartY + 54));  
        
        this.addSlot(new Slot(equipWrapper, 0, equipX, handsStartY));       
        this.addSlot(new Slot(equipWrapper, 1, equipX, handsStartY + 30));  

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
    }

    public int getRowCount() {
        return this.containerRows;
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
                EquipmentSlot equipSlot = Mob.getEquipmentSlotForItem(slotStack);
                
                for (int i = 0; i < 6; i++) {
                    Slot s = this.slots.get(i);
                    int internalId = s.getSlotIndex();
                    
                    if ((equipSlot == EquipmentSlot.HEAD && internalId == 5) ||
                        (equipSlot == EquipmentSlot.CHEST && internalId == 4) ||
                        (equipSlot == EquipmentSlot.LEGS && internalId == 3) ||
                        (equipSlot == EquipmentSlot.FEET && internalId == 2) ||
                        (slotStack.is(Items.SHIELD) && internalId == 1) ||
                        ((slotStack.getItem() instanceof TieredItem || 
                          slotStack.getItem() instanceof SwordItem || 
                          slotStack.getItem() instanceof BowItem || 
                          slotStack.getItem() instanceof CrossbowItem || 
                          slotStack.getItem() instanceof TridentItem) && internalId == 0)) {
                        
                        if (!s.hasItem()) {
                            movedToEquip = this.moveItemStackTo(slotStack, i, i + 1, false);
                            break;
                        }
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