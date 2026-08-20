package com.solarbiscuit.compat.sophisticatedstorage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlock;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;

public final class FemboyChestStorageWrapper extends StackStorageWrapper {
    private final ItemStack chest;

    public FemboyChestStorageWrapper(ItemStack chest) {
        super(chest);
        this.chest = chest;
    }

    @Override
    protected boolean isAllowedInStorage(ItemStack stack) {
        if (!(this.chest.getItem() instanceof ShulkerBoxItem)) {
            return true;
        }
        Block block = Block.byItem(stack.getItem());
        if (block instanceof ShulkerBoxBlock || block instanceof net.minecraft.world.level.block.ShulkerBoxBlock) {
            return false;
        }
        return !Config.SERVER.shulkerBoxDisallowedItems.isItemDisallowed(stack.getItem());
    }
}
