package com.solarbiscuit.compat.sophisticatedbackpacks;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

public class FemboyBackpackSettingsMenu extends SettingsContainerMenu<IBackpackWrapper> {
    private final FemboyEntity femboy;

    public FemboyBackpackSettingsMenu(int id, Player player, FemboyEntity femboy, IBackpackWrapper wrapper) {
        super(FemboyBackpackAccess.SETTINGS_MENU.get(), id, player, wrapper);
        this.femboy = femboy;
    }

    public static FemboyBackpackSettingsMenu fromBuffer(int id, Inventory inv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        var backpack = buf.readItem();
        Entity entity = inv.player.level().getEntity(entityId);
        if (!(entity instanceof FemboyEntity femboy)) {
            throw new IllegalStateException("Femboy entity not found for backpack settings: " + entityId);
        }
        IBackpackWrapper wrapper = FemboyBackpackAccess.wrapperFrom(
                inv.player.level().isClientSide ? backpack : FemboyBackpackAccess.getBackpack(femboy));
        return new FemboyBackpackSettingsMenu(id, inv.player, femboy, wrapper);
    }

    public FemboyEntity getFemboy() {
        return this.femboy;
    }

    @Override
    public void detectSettingsChangeAndReload() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.femboy.isAlive() && this.femboy.distanceTo(player) < 8.0F && this.femboy.isOwnedBy(player)
                && !FemboyBackpackAccess.getBackpack(this.femboy).isEmpty();
    }

    @Override
    public void handleMessage(CompoundTag data) {
        if ("openBackpack".equals(data.getString("action")) && this.getPlayer() instanceof ServerPlayer serverPlayer) {
            FemboyBackpackAccess.open(serverPlayer, this.femboy);
            return;
        }
        super.handleMessage(data);
    }
}
