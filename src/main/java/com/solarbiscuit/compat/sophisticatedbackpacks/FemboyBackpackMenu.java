package com.solarbiscuit.compat.sophisticatedbackpacks;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.RightClickOpenSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

import java.util.Optional;

public class FemboyBackpackMenu extends StorageContainerMenuBase<IBackpackWrapper> implements ISyncedContainer {
    private final FemboyEntity femboy;

    public FemboyBackpackMenu(int id, Player player, FemboyEntity femboy, IBackpackWrapper wrapper) {
        super(FemboyBackpackAccess.BACKPACK_MENU.get(), id, player, wrapper, NoopStorageWrapper.INSTANCE,
                -1, false);
        this.femboy = femboy;
    }

    public static FemboyBackpackMenu fromBuffer(int id, Inventory inv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        var backpack = buf.readItem();
        Entity entity = inv.player.level().getEntity(entityId);
        if (!(entity instanceof FemboyEntity femboy)) {
            throw new IllegalStateException("Femboy entity not found for backpack GUI: " + entityId);
        }
        IBackpackWrapper wrapper = inv.player.level().isClientSide
                ? FemboyBackpackAccess.wrapperFrom(backpack)
                : FemboyBackpackAccess.wrapperFrom(FemboyBackpackAccess.getBackpack(femboy));
        return new FemboyBackpackMenu(id, inv.player, femboy, wrapper);
    }

    public FemboyEntity getFemboy() {
        return this.femboy;
    }

    @Override
    public Optional<BlockPos> getBlockPosition() {
        return Optional.empty();
    }

    @Override
    public Optional<Entity> getEntity() {
        return Optional.of(this.femboy);
    }

    @Override
    protected StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
        return new StorageUpgradeSlot(upgradeHandler, slotIndex);
    }

    @Override
    public void openSettings() {
        if (isClientSide()) {
            sendToServer(tag -> tag.putString("action", "openSettings"));
            return;
        }
        if (this.player instanceof ServerPlayer serverPlayer) {
            FemboyBackpackAccess.openSettings(serverPlayer, this.femboy);
        }
    }

    @Override
    public void handleMessage(CompoundTag data) {
        if ("openSettings".equals(data.getString("action"))) {
            openSettings();
            return;
        }
        super.handleMessage(data);
    }

    @Override
    protected boolean storageItemHasChanged() {
        return false;
    }

    @Override
    public boolean detectSettingsChangeAndReload() {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.femboy.isAlive() && this.femboy.distanceTo(player) < 8.0F && this.femboy.isOwnedBy(player)
                && !FemboyBackpackAccess.getBackpack(this.femboy).isEmpty();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size() && button == 1 && clickType == ClickType.PICKUP) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof RightClickOpenSlot openSlot && openSlot.openFor(player)) {
                return;
            }
        }
        try {
            super.clicked(slotId, button, clickType, player);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }
}
