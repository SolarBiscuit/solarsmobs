package com.solarbiscuit.compat.sophisticatedbackpacks;

import net.minecraft.client.gui.screens.MenuScreens;

public final class FemboyBackpackClient {
    private FemboyBackpackClient() {}

    public static void registerScreens() {
        MenuScreens.register(FemboyBackpackAccess.BACKPACK_MENU.get(), FemboyBackpackScreen::new);
        MenuScreens.register(FemboyBackpackAccess.SETTINGS_MENU.get(), FemboyBackpackSettingsScreen::new);
    }
}
