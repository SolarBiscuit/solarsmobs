package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SolarsMobs.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.solarsmobs"))
            .icon(() -> new ItemStack(ModItems.HOLY_CROSS.get()))
            .displayItems((params, output) -> {
                output.accept(ModEntities.FEMBOY_SPAWN_EGG.get());
                output.accept(ModEntities.THIEF_SPAWN_EGG.get());
                output.accept(ModEntities.TEMPLAR_SPAWN_EGG.get());
                output.accept(ModEntities.END_WARRIOR_SPAWN_EGG.get());
                output.accept(ModItems.FEMBOY_MILK_BUCKET.get());
                output.accept(ModItems.THIEVES_GUILD_NECKLACE.get());
                output.accept(ModItems.HOLY_CROSS.get());
                output.accept(ModItems.SACRED_ENDER_RING.get());
            })
            .build());
}
