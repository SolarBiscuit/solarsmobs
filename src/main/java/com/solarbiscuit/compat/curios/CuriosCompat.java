package com.solarbiscuit.compat.curios;

import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;

/**
 * Soft Curios bridge. Safe when Curios is absent at runtime.
 * Slot assignment is datapack-driven; see data/curios and data/solarsmobs/curios.
 * Docs: https://docs.illusivesoulworks.com/category/curios
 */
public final class CuriosCompat {
    public static final String MOD_ID = "curios";
    public static final String BACK_SLOT = "back";
    public static final String HEAD_SLOT = "head";

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!isLoaded() || entity == null || item == null) {
            return false;
        }
        LazyOptional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(entity);
        return curios.map(inv -> inv.isEquipped(item)).orElse(false);
    }

    public static boolean canEquip(LivingEntity entity, String slotId, ItemStack stack) {
        if (!isLoaded() || entity == null || stack.isEmpty()) {
            return false;
        }
        return CuriosApi.getItemStackSlots(stack, entity).containsKey(slotId);
    }

    public static ItemStack getStack(LivingEntity entity, String slotId) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getStacksHandler(slotId)
                        .map(stacks -> stacks.getStacks().getStackInSlot(0))
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    public static void setStack(LivingEntity entity, String slotId, ItemStack stack) {
        if (!isLoaded()) {
            return;
        }
        CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
                handler.getStacksHandler(slotId).ifPresent(stacks ->
                        stacks.getStacks().setStackInSlot(0, stack)));
    }

    public static void addSlots(List<Slot> extra, FemboyEntity femboy, int x, int backY) {
        if (!isLoaded()) {
            return;
        }
        extra.add(createSlot(femboy, BACK_SLOT, x, backY, emptyIcon(BACK_SLOT)));
        extra.add(createSlot(femboy, HEAD_SLOT, x, backY + 18, emptyIcon(HEAD_SLOT)));
    }

    public static Slot createSlot(FemboyEntity femboy, String slotId, int x, int y, ResourceLocation icon) {
        FemboyCuriosSlot slot = new FemboyCuriosSlot(femboy, slotId, x, y);
        if (icon != null) {
            slot.setBackground(InventoryMenu.BLOCK_ATLAS, icon);
        }
        return slot;
    }

    public static void dropFemboySlots(FemboyEntity femboy) {
        dropSlot(femboy, BACK_SLOT);
        dropSlot(femboy, HEAD_SLOT);
    }

    public static void dropSlot(FemboyEntity femboy, String slotId) {
        ItemStack stack = getStack(femboy, slotId);
        if (!stack.isEmpty()) {
            femboy.spawnAtLocation(stack);
            setStack(femboy, slotId, ItemStack.EMPTY);
        }
    }

    public static ResourceLocation emptyIcon(String slotId) {
        return new ResourceLocation(MOD_ID, "slot/empty_" + slotId + "_slot");
    }

    public static boolean openBackIfBackpack(FemboyEntity femboy, net.minecraft.world.entity.player.Player player) {
        return FemboyBackpackCompat.isLoaded() && FemboyBackpackCompat.handleOpen(player, femboy);
    }
}
