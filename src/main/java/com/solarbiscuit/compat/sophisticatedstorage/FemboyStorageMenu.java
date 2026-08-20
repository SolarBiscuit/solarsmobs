package com.solarbiscuit.compat.sophisticatedstorage;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.FemboyEquipmentContainer;
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
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

import java.util.Optional;

public class FemboyStorageMenu extends StorageContainerMenuBase<IStorageWrapper> implements ISyncedContainer {
    private final FemboyEntity femboy;
    private final Player opener;

    public FemboyStorageMenu(int id, Player player, FemboyEntity femboy, IStorageWrapper wrapper) {
        super(FemboyStorageAccess.STORAGE_MENU.get(), id, player, wrapper, NoopStorageWrapper.INSTANCE,
                -1, false, FemboyStorageAccess.extraSlots(femboy));
        this.femboy = femboy;
        this.opener = player;
    }

    public static FemboyStorageMenu fromBuffer(int id, Inventory inv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        var chest = buf.readItem();
        Entity entity = inv.player.level().getEntity(entityId);
        if (!(entity instanceof FemboyEntity femboy)) {
            throw new IllegalStateException("Femboy entity not found for SS GUI: " + entityId);
        }
        IStorageWrapper wrapper = inv.player.level().isClientSide
                ? FemboyStorageAccess.clientWrapper(chest)
                : FemboyStorageAccess.serverWrapper(femboy);
        return new FemboyStorageMenu(id, inv.player, femboy, wrapper);
    }

    public FemboyEntity getFemboy() {
        return this.femboy;
    }

    private int extraSlotCount() {
        return this.getExtraSlots().size();
    }

    private int extraSlotStart() {
        return this.realInventorySlots.size() - extraSlotCount();
    }

    @Override
    public Slot getSlot(int slotId) {
        if (slotId >= 0 && slotId < this.realInventorySlots.size()) {
            return this.realInventorySlots.get(slotId);
        }
        int upgradeIndex = slotId - this.realInventorySlots.size();
        if (upgradeIndex >= 0 && upgradeIndex < this.upgradeSlots.size()) {
            return this.upgradeSlots.get(upgradeIndex);
        }
        return super.getSlot(slotId);
    }

    @Override
    protected void refreshInventorySlotsIfNeeded() {
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
        if (this.opener instanceof ServerPlayer serverPlayer) {
            FemboyStorageAccess.openSettings(serverPlayer, this.femboy);
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
        return this.femboy.isAlive() && this.femboy.distanceTo(player) < 8.0F && this.femboy.isOwnedBy(player);
    }

    @Override
    public void broadcastChanges() {
        try {
            super.broadcastChanges();
        } catch (IndexOutOfBoundsException ignored) {
        }
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isUpgradeSlot(index) || isUpgradeSettingsSlot(index) || index >= getFirstUpgradeSlot()) {
            try {
                return super.quickMoveStack(player, index);
            } catch (IndexOutOfBoundsException ignored) {
                return ItemStack.EMPTY;
            }
        }
        Slot slot = getSlot(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        int storageEnd = getNumberOfStorageInventorySlots();
        int extraStart = extraSlotStart();
        int extraEnd = this.realInventorySlots.size();
        boolean moved;
        boolean upgradeItem = current.getItem() instanceof IUpgradeItem;
        if (index < storageEnd) {
            moved = this.moveItemStackTo(slot, current, storageEnd, extraStart, true);
            if (!moved && !upgradeItem) {
                moved = this.mergeItemStack(slot, current, extraStart, extraEnd, true, true);
            }
        } else if (index >= extraStart && index < extraEnd) {
            moved = this.moveItemStackTo(slot, current, storageEnd, extraStart, true)
                    || mergeToStorage(slot, current, storageEnd);
        } else {
            moved = false;
            if (upgradeItem) {
                moved = this.moveItemStackTo(slot, current, getFirstUpgradeSlot(), getTotalSlotsNumber(), false);
            }
            if (!moved && (FemboyEquipmentContainer.isHandheldLight(current) || FemboyEquipmentContainer.isShield(current))) {
                int offhand = extraStart + 5;
                if (offhand < extraEnd) {
                    moved = this.mergeItemStack(slot, current, offhand, offhand + 1, false, true);
                }
            }
            if (!moved && !upgradeItem) {
                moved = this.mergeItemStack(slot, current, extraStart, extraEnd, false, true);
            }
            if (!moved) {
                moved = mergeToStorage(slot, current, storageEnd);
            }
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = slot.getItem();
        if (remaining.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (remaining.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, remaining);
        return original;
    }

    private boolean mergeToStorage(Slot slot, ItemStack current, int storageEnd) {
        ItemStack leftover = this.mergeItemStack(current, 0, storageEnd, false, false, true);
        if (leftover.getCount() != current.getCount()) {
            slot.set(leftover);
            return true;
        }
        return false;
    }
}
