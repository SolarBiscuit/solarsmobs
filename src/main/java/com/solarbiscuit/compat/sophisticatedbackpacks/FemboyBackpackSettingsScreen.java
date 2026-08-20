package com.solarbiscuit.compat.sophisticatedbackpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackToBackpackTab;
import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackSettingsTabControl;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;

public class FemboyBackpackSettingsScreen extends BackpackSettingsScreen {
    public FemboyBackpackSettingsScreen(SettingsContainerMenu<?> menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected StorageSettingsTabControlBase initializeTabControl() {
        return new FemboyBackpackSettingsTabControl(this, new Position(this.leftPos + this.imageWidth, this.topPos + 4));
    }

    @Override
    protected void sendStorageInventoryScreenOpenMessage() {
        this.getMenu().sendDataToServer(() -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("action", "openBackpack");
            return tag;
        });
    }

    private static final class FemboyBackpackSettingsTabControl extends BackpackSettingsTabControl {
        FemboyBackpackSettingsTabControl(FemboyBackpackSettingsScreen screen, Position position) {
            super(screen, position);
        }

        @Override
        protected Tab instantiateReturnBackTab() {
            return new FemboyBackToBackpackTab(new Position(this.x, this.getTopY()), (FemboyBackpackSettingsScreen) this.screen);
        }
    }

    private static final class FemboyBackToBackpackTab extends BackToBackpackTab {
        private final FemboyBackpackSettingsScreen owner;

        FemboyBackToBackpackTab(Position position, FemboyBackpackSettingsScreen owner) {
            super(position);
            this.owner = owner;
        }

        @Override
        protected void onTabIconClicked(int button) {
            if (this.owner != null) {
                this.owner.sendStorageInventoryScreenOpenMessage();
            }
        }
    }
}
