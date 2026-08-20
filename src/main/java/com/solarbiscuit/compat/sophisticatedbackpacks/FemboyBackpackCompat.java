package com.solarbiscuit.compat.sophisticatedbackpacks;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.List;

public final class FemboyBackpackCompat {
    public static final String MOD_ID = "sophisticatedbackpacks";

    private FemboyBackpackCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void register(IEventBus modBus) {
        if (isLoaded()) {
            ensureBackpackSettingsInitialized();
            FemboyBackpackAccess.register(modBus);
        }
    }

    static void ensureBackpackSettingsInitialized() {
        try {
            Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackSettingsContainerMenu");
            Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackSettingsTabControl");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static InteractionResult tryInteract(FemboyEntity femboy, Player player, InteractionHand hand, ItemStack stack) {
        if (!isLoaded()) {
            return InteractionResult.PASS;
        }
        return FemboyBackpackAccess.tryInteract(femboy, player, hand, stack);
    }

    public static void addSlot(List<Slot> extra, FemboyEntity femboy, int x, int y) {
        if (isLoaded()) {
            FemboyBackpackAccess.addSlot(extra, femboy, x, y);
        }
    }

    public static Slot createSlot(FemboyEntity femboy, int x, int y) {
        return isLoaded() ? FemboyBackpackAccess.createSlot(femboy, x, y) : null;
    }

    public static boolean handleClick(Slot slot, Player player) {
        return isLoaded() && FemboyBackpackAccess.handleClick(slot, player);
    }

    public static boolean handleOpen(Player player, FemboyEntity femboy) {
        if (!isLoaded()) {
            return false;
        }
        if (!FemboyBackpackAccess.isBackpackStack(FemboyBackpackAccess.getBackpack(femboy))) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            FemboyBackpackAccess.open(serverPlayer, femboy);
            return true;
        }
        return player.level().isClientSide;
    }

    public static void open(Player player, FemboyEntity femboy) {
        if (isLoaded() && player instanceof ServerPlayer serverPlayer) {
            FemboyBackpackAccess.open(serverPlayer, femboy);
        }
    }

    public static void tick(FemboyEntity femboy) {
        if (isLoaded()) {
            FemboyBackpackAccess.tick(femboy);
        }
    }

    public static void dropBackpack(FemboyEntity femboy) {
        if (isLoaded()) {
            FemboyBackpackAccess.dropBackpack(femboy);
        }
    }

    public static boolean isOpenFor(Player player, FemboyEntity femboy) {
        if (!isLoaded()) {
            return false;
        }
        return player.containerMenu instanceof FemboyBackpackMenu menu && menu.getFemboy() == femboy
                || player.containerMenu instanceof FemboyBackpackSettingsMenu settings && settings.getFemboy() == femboy;
    }
}
