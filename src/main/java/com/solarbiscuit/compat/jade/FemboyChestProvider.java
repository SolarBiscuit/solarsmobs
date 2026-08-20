package com.solarbiscuit.compat.jade;

import com.solarbiscuit.compat.sophisticatedstorage.FemboyStorageCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum FemboyChestProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final String CHEST_TAG = "Chest";
    private static final String LARGE_CHEST_TAG = "LargeChest";

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof FemboyEntity femboy)) {
            return;
        }
        ItemStack storageChest = FemboyStorageCompat.peekChestStack(femboy);
        if (!storageChest.isEmpty()) {
            data.put(CHEST_TAG, storageChest.save(new CompoundTag()));
            return;
        }
        data.putBoolean(LARGE_CHEST_TAG, femboy.isLargeChest());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        IElementHelper helper = tooltip.getElementHelper();
        if (data.contains(CHEST_TAG)) {
            ItemStack chest = ItemStack.of(data.getCompound(CHEST_TAG));
            if (!chest.isEmpty()) {
                tooltip.add(helper.smallItem(chest));
                tooltip.append(chest.getHoverName());
                return;
            }
        }
        ItemStack vanillaChest = new ItemStack(Items.CHEST);
        tooltip.add(helper.smallItem(vanillaChest));
        if (data.getBoolean(LARGE_CHEST_TAG)) {
            tooltip.append(Component.translatable("jade.solarsmobs.femboy.large_chest"));
        } else {
            tooltip.append(vanillaChest.getHoverName());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return SolarsMobsJadePlugin.FEMBOY_CHEST;
    }
}
