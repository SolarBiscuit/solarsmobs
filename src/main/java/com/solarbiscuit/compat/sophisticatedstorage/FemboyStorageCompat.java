package com.solarbiscuit.compat.sophisticatedstorage;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

public final class FemboyStorageCompat {
    public static final String STORAGE_MOD_ID = "sophisticatedstorage";
    public static final String CORE_MOD_ID = "sophisticatedcore";

    private FemboyStorageCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(STORAGE_MOD_ID) && ModList.get().isLoaded(CORE_MOD_ID);
    }

    public static void register(IEventBus modBus) {
        if (isLoaded()) {
            FemboyStorageAccess.register(modBus);
        }
    }

    public static void open(Player player, FemboyEntity femboy) {
        if (isLoaded() && player instanceof ServerPlayer serverPlayer) {
            FemboyStorageAccess.open(serverPlayer, femboy);
        }
    }

    public static InteractionResult tryInteract(FemboyEntity femboy, Player player, InteractionHand hand, ItemStack stack) {
        if (!isLoaded()) {
            return InteractionResult.PASS;
        }
        return FemboyStorageAccess.tryInteract(femboy, player, hand, stack);
    }

    public static void save(FemboyEntity femboy, CompoundTag tag) {
        if (isLoaded()) {
            FemboyStorageAccess.save(femboy, tag);
        }
    }

    public static void load(FemboyEntity femboy, CompoundTag tag) {
        if (isLoaded()) {
            FemboyStorageAccess.load(femboy, tag);
        }
    }

    public static void dropContents(FemboyEntity femboy) {
        if (isLoaded()) {
            FemboyStorageAccess.dropContents(femboy);
        }
    }

    public static void tick(FemboyEntity femboy) {
        if (isLoaded()) {
            FemboyStorageAccess.tick(femboy);
        }
    }

    public static boolean isOpenFor(Player player, FemboyEntity femboy) {
        return isLoaded() && FemboyStorageAccess.isOpenFor(player, femboy);
    }

    public static ItemStack peekChestStack(FemboyEntity femboy) {
        return isLoaded() ? FemboyStorageAccess.peekChestStack(femboy) : ItemStack.EMPTY;
    }
}
