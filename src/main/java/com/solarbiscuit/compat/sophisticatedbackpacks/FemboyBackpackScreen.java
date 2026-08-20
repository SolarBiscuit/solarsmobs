package com.solarbiscuit.compat.sophisticatedbackpacks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public class FemboyBackpackScreen extends StorageScreenBase<FemboyBackpackMenu> {
    public FemboyBackpackScreen(FemboyBackpackMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return "Backpack Settings";
    }
}
