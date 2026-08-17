package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.FemboyMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SolarsMobs.MOD_ID);

    public static final RegistryObject<MenuType<FemboyMenu>> FEMBOY_MENU =
            MENUS.register("femboy_menu", () -> IForgeMenuType.create((windowId, inv, data) -> {
                int entityId = data.readInt();
                boolean isLarge = data.readBoolean();
                net.minecraft.world.entity.Entity entity = inv.player.level().getEntity(entityId);

                if (entity instanceof FemboyEntity femboyEntity) {
                    return new FemboyMenu(windowId, inv, femboyEntity, isLarge);
                }
                throw new IllegalStateException("Femboy entity not found for GUI: " + entityId);
            }));
}
