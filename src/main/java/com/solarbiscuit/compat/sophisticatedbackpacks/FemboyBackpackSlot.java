package com.solarbiscuit.compat.sophisticatedbackpacks;

import com.solarbiscuit.compat.curios.CuriosCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.RightClickOpenSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FemboyBackpackSlot extends Slot implements RightClickOpenSlot {
    private final FemboyEntity femboy;

    public FemboyBackpackSlot(FemboyEntity femboy, int x, int y) {
        super(new SimpleContainer(1) {
            @Override
            public ItemStack getItem(int slot) {
                return FemboyBackpackAccess.getBackpack(femboy);
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                FemboyBackpackAccess.setBackpack(femboy, stack);
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
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CuriosCompat.canEquip(this.femboy, CuriosCompat.BACK_SLOT, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean openFor(Player player) {
        if (player instanceof ServerPlayer serverPlayer && !getItem().isEmpty()) {
            FemboyBackpackAccess.open(serverPlayer, this.femboy);
            return true;
        }
        return !getItem().isEmpty() && player.level().isClientSide;
    }
}
