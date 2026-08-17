package com.solarbiscuit.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * Soft Curios bridge. Safe when Curios is absent at runtime.
 * Slot assignment is datapack-driven; see data/curios and data/solarsmobs/curios.
 * Docs: https://docs.illusivesoulworks.com/category/curios
 */
public final class CuriosCompat {
    public static final String MOD_ID = "curios";

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
}
