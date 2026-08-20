package com.solarbiscuit.compat.sophisticatedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.BackToStorageTab;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsTabControl;

public class FemboyStorageSettingsScreen extends StorageSettingsScreen {
    public FemboyStorageSettingsScreen(SettingsContainerMenu<?> menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected StorageSettingsTabControlBase initializeTabControl() {
        return new FemboySettingsTabControl(this, new Position(this.leftPos + this.imageWidth, this.topPos + 4));
    }

    @Override
    protected void sendStorageInventoryScreenOpenMessage() {
        this.getMenu().sendDataToServer(() -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("action", "openStorage");
            return tag;
        });
    }

    private static final class FemboySettingsTabControl extends StorageSettingsTabControl {
        FemboySettingsTabControl(FemboyStorageSettingsScreen screen, Position position) {
            super(screen, position);
        }

        @Override
        protected Tab instantiateReturnBackTab() {
            return new FemboyBackToStorageTab(new Position(this.x, this.getTopY()), (FemboyStorageSettingsScreen) this.screen);
        }
    }

    private static final class FemboyBackToStorageTab extends BackToStorageTab {
        private final FemboyStorageSettingsScreen owner;

        FemboyBackToStorageTab(Position position, FemboyStorageSettingsScreen owner) {
            super(position, BlockPos.ZERO);
            this.owner = owner;
        }

        @Override
        protected void onTabIconClicked(int button) {
            this.owner.sendStorageInventoryScreenOpenMessage();
        }
    }
}
