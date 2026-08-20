package com.solarbiscuit.compat.sophisticatedstorage;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

public class FemboyStorageSettingsMenu extends SettingsContainerMenu<IStorageWrapper> {
    private final FemboyEntity femboy;

    public FemboyStorageSettingsMenu(int id, Player player, FemboyEntity femboy, IStorageWrapper wrapper) {
        super(FemboyStorageAccess.SETTINGS_MENU.get(), id, player, wrapper);
        this.femboy = femboy;
    }

    public static FemboyStorageSettingsMenu fromBuffer(int id, Inventory inv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        var chest = buf.readItem();
        Entity entity = inv.player.level().getEntity(entityId);
        if (!(entity instanceof FemboyEntity femboy)) {
            throw new IllegalStateException("Femboy entity not found for SS settings: " + entityId);
        }
        IStorageWrapper wrapper = inv.player.level().isClientSide
                ? FemboyStorageAccess.clientWrapper(chest)
                : FemboyStorageAccess.serverWrapper(femboy);
        return new FemboyStorageSettingsMenu(id, inv.player, femboy, wrapper);
    }

    public FemboyEntity getFemboy() {
        return this.femboy;
    }

    @Override
    public void detectSettingsChangeAndReload() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.femboy.isAlive() && this.femboy.distanceTo(player) < 8.0F && this.femboy.isOwnedBy(player);
    }

    @Override
    public void handleMessage(CompoundTag data) {
        if ("openStorage".equals(data.getString("action")) && this.getPlayer() instanceof ServerPlayer serverPlayer) {
            FemboyStorageAccess.open(serverPlayer, this.femboy);
            return;
        }
        super.handleMessage(data);
    }
}
