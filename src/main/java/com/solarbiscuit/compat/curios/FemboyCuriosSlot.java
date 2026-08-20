package com.solarbiscuit.compat.curios;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.RightClickOpenSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FemboyCuriosSlot extends Slot implements RightClickOpenSlot {
    private final FemboyEntity femboy;
    private final String slotId;

    public FemboyCuriosSlot(FemboyEntity femboy, String slotId, int x, int y) {
        super(new SimpleContainer(1) {
            @Override
            public ItemStack getItem(int slot) {
                return CuriosCompat.getStack(femboy, slotId);
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                CuriosCompat.setStack(femboy, slotId, stack);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack current = getItem(0);
                if (current.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack taken = current.split(amount);
                setItem(0, current);
                return taken;
            }
        }, 0, x, y);
        this.femboy = femboy;
        this.slotId = slotId;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CuriosCompat.canEquip(this.femboy, this.slotId, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean openFor(Player player) {
        if (!CuriosCompat.BACK_SLOT.equals(this.slotId) || getItem().isEmpty()) {
            return false;
        }
        if (player instanceof ServerPlayer) {
            return CuriosCompat.openBackIfBackpack(this.femboy, player);
        }
        return player.level().isClientSide;
    }
}
