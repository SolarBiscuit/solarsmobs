package com.solarbiscuit.compat.sophisticatedstorage;

import net.minecraft.client.gui.screens.MenuScreens;

public final class FemboyStorageClient {
    private FemboyStorageClient() {}

    public static void registerScreens() {
        MenuScreens.register(FemboyStorageAccess.STORAGE_MENU.get(), FemboyStorageScreen::new);
        MenuScreens.register(FemboyStorageAccess.SETTINGS_MENU.get(), FemboyStorageSettingsScreen::new);
    }
}
