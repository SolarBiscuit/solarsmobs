package com.solarbiscuit.compat.sophisticatedbackpacks;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.RightClickOpenSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IPickupResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;

public final class FemboyBackpackAccess {
    public static final String BACK_SLOT = "back";

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SolarsMobs.MOD_ID);

    public static final RegistryObject<MenuType<FemboyBackpackMenu>> BACKPACK_MENU =
            MENUS.register("femboy_sb_backpack", () -> IForgeMenuType.create(FemboyBackpackMenu::fromBuffer));
    public static final RegistryObject<MenuType<FemboyBackpackSettingsMenu>> SETTINGS_MENU =
            MENUS.register("femboy_sb_settings", () -> IForgeMenuType.create(FemboyBackpackSettingsMenu::fromBuffer));

    private FemboyBackpackAccess() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static InteractionResult tryInteract(FemboyEntity femboy, Player player, InteractionHand hand, ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return InteractionResult.PASS;
        }
        ItemStack equipped = getBackpack(femboy);
        if (equipped.isEmpty()) {
            setBackpack(femboy, stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            femboy.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        ItemStack swapped = equipped.copy();
        setBackpack(femboy, stack.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            player.setItemInHand(hand, swapped);
        } else if (!player.getInventory().add(swapped)) {
            player.drop(swapped, false);
        }
        femboy.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    public static void addSlot(List<Slot> extra, FemboyEntity femboy, int x, int y) {
        extra.add(createSlot(femboy, x, y));
    }

    public static Slot createSlot(FemboyEntity femboy, int x, int y) {
        return new FemboyBackpackSlot(femboy, x, y);
    }

    public static boolean handleClick(Slot slot, Player player) {
        return slot instanceof RightClickOpenSlot openSlot && openSlot.openFor(player);
    }

    public static void open(ServerPlayer player, FemboyEntity femboy) {
        ItemStack backpack = getBackpack(femboy);
        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
            return;
        }
        IBackpackWrapper wrapper = new BackpackWrapper(backpack);
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return wrapper.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player opener) {
                return new FemboyBackpackMenu(id, opener, femboy, wrapper);
            }
        }, buf -> {
            buf.writeVarInt(femboy.getId());
            buf.writeItem(backpack);
        });
    }

    public static void openSettings(ServerPlayer player, FemboyEntity femboy) {
        FemboyBackpackCompat.ensureBackpackSettingsInitialized();
        ItemStack backpack = getBackpack(femboy);
        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
            return;
        }
        IBackpackWrapper wrapper = new BackpackWrapper(backpack);
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return wrapper.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player opener) {
                return new FemboyBackpackSettingsMenu(id, opener, femboy, wrapper);
            }
        }, buf -> {
            buf.writeVarInt(femboy.getId());
            buf.writeItem(backpack);
        });
    }

    public static void tick(FemboyEntity femboy) {
        if (femboy.level().isClientSide) {
            return;
        }
        ItemStack backpack = getBackpack(femboy);
        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
            return;
        }
        IBackpackWrapper wrapper = new BackpackWrapper(backpack);
        Level level = femboy.level();
        BlockPos pos = femboy.blockPosition();
        for (ITickableUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)) {
            upgrade.tick(femboy, level, pos);
        }
        List<IPickupResponseUpgrade> pickups = wrapper.getUpgradeHandler().getWrappersThatImplement(IPickupResponseUpgrade.class);
        if (pickups.isEmpty()) {
            return;
        }
        AABB box = femboy.getBoundingBox().inflate(0.75D);
        Player owner = femboy.getOwner() instanceof Player player ? player : null;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!itemEntity.isAlive()) {
                continue;
            }
            ItemStack remaining = itemEntity.getItem();
            if (owner != null) {
                remaining = InventoryHelper.runPickupOnPickupResponseUpgrades(level, owner, wrapper.getUpgradeHandler(), remaining, false);
            } else {
                remaining = InventoryHelper.runPickupOnPickupResponseUpgrades(level, wrapper.getUpgradeHandler(), remaining, false);
            }
            remaining = InventoryHelper.insertIntoInventory(remaining, wrapper.getInventoryForInputOutput(), false);
            itemEntity.setItem(remaining);
            if (remaining.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public static void dropBackpack(FemboyEntity femboy) {
        ItemStack backpack = getBackpack(femboy);
        if (!backpack.isEmpty()) {
            femboy.spawnAtLocation(backpack);
            setBackpack(femboy, ItemStack.EMPTY);
        }
    }

    static boolean isBackpackStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BackpackItem;
    }

    static ItemStack getBackpack(FemboyEntity femboy) {
        return curios(femboy).map(handler -> handler.getStacksHandler(BACK_SLOT)
                .map(stacks -> stacks.getStacks().getStackInSlot(0))
                .orElse(ItemStack.EMPTY)).orElse(ItemStack.EMPTY);
    }

    static void setBackpack(FemboyEntity femboy, ItemStack stack) {
        curios(femboy).ifPresent(handler -> handler.getStacksHandler(BACK_SLOT).ifPresent(stacks ->
                stacks.getStacks().setStackInSlot(0, stack)));
    }

    static IBackpackWrapper wrapperFrom(ItemStack stack) {
        return new BackpackWrapper(stack);
    }

    private static LazyOptional<ICuriosItemHandler> curios(FemboyEntity femboy) {
        return CuriosApi.getCuriosInventory(femboy);
    }
}
