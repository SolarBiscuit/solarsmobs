package com.solarbiscuit.compat.sophisticatedstorage;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.compat.curios.CuriosCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.inventory.femboy.FemboyEquipmentContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IPickupResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.IStorageBlock;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("removal")
public final class FemboyStorageAccess {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SolarsMobs.MOD_ID);

    public static final RegistryObject<MenuType<FemboyStorageMenu>> STORAGE_MENU =
            MENUS.register("femboy_ss_storage", () -> IForgeMenuType.create(FemboyStorageMenu::fromBuffer));
    public static final RegistryObject<MenuType<FemboyStorageSettingsMenu>> SETTINGS_MENU =
            MENUS.register("femboy_ss_settings", () -> IForgeMenuType.create(FemboyStorageSettingsMenu::fromBuffer));

    private static final String NBT_KEY = "sophisticatedStorage";
    private static final String MIGRATED_KEY = "SSMigrated";
    private static final ResourceLocation BASIC_CHEST_ID = new ResourceLocation("sophisticatedstorage", "chest");

    private static final Map<FemboyEntity, Holder> HOLDERS = new WeakHashMap<>();

    private FemboyStorageAccess() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static void open(ServerPlayer player, FemboyEntity femboy) {
        Holder holder = getOrCreate(femboy);
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return holder.wrapper.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player opener) {
                return new FemboyStorageMenu(id, opener, femboy, holder.wrapper);
            }
        }, buf -> {
            buf.writeVarInt(femboy.getId());
            buf.writeItem(holder.stack);
        });
    }

    public static void openSettings(ServerPlayer player, FemboyEntity femboy) {
        Holder holder = getOrCreate(femboy);
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.sophisticatedstorage.settings.title", holder.wrapper.getDisplayName());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player opener) {
                return new FemboyStorageSettingsMenu(id, opener, femboy, holder.wrapper);
            }
        }, buf -> {
            buf.writeVarInt(femboy.getId());
            buf.writeItem(holder.stack);
        });
    }

    public static InteractionResult tryInteract(FemboyEntity femboy, Player player, InteractionHand hand, ItemStack stack) {
        if (stack.getItem() instanceof StorageTierUpgradeItem upgradeItem) {
            if (tryTierUpgrade(femboy, player, stack, upgradeItem)) {
                return InteractionResult.SUCCESS;
            }
        }
        if (stack.getItem() instanceof IUpgradeItem<?>) {
            if (tryInsertUpgrade(femboy, player, stack)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void save(FemboyEntity femboy, CompoundTag tag) {
        Holder holder = HOLDERS.get(femboy);
        if (holder != null) {
            tag.put(NBT_KEY, holder.stack.save(new CompoundTag()));
            tag.putBoolean(MIGRATED_KEY, true);
        }
    }

    public static void load(FemboyEntity femboy, CompoundTag tag) {
        if (tag.contains(NBT_KEY)) {
            ItemStack stack = ItemStack.of(tag.getCompound(NBT_KEY));
            HOLDERS.put(femboy, new Holder(stack, new FemboyChestStorageWrapper(stack)));
            SimpleContainer vanilla = femboy.getInventory();
            for (int i = 0; i < vanilla.getContainerSize(); i++) {
                vanilla.setItem(i, ItemStack.EMPTY);
            }
            return;
        }
        getOrCreate(femboy);
    }

    public static void dropContents(FemboyEntity femboy) {
        Holder holder = getOrCreate(femboy);
        dropTierUpgradeRefund(femboy, holder.stack);
        InventoryHandler inv = holder.wrapper.getInventoryHandler();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                femboy.spawnAtLocation(stack);
                inv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        UpgradeHandler upgrades = holder.wrapper.getUpgradeHandler();
        for (int i = 0; i < upgrades.getSlots(); i++) {
            ItemStack stack = upgrades.getStackInSlot(i);
            if (!stack.isEmpty()) {
                femboy.spawnAtLocation(stack);
                upgrades.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public static void tick(FemboyEntity femboy) {
        if (femboy.level().isClientSide) {
            return;
        }
        Holder holder = HOLDERS.get(femboy);
        if (holder == null) {
            return;
        }
        IStorageWrapper wrapper = holder.wrapper;
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

    static final int EXTRA_SLOT_X = -54;
    static final int GEAR_SLOT_Y = 8;
    static final int CURIOS_SLOT_Y = 130;

    static List<Slot> extraSlots(FemboyEntity femboy) {
        List<Slot> extra = new ArrayList<>(FemboyEquipmentContainer.stackedSlots(femboy, EXTRA_SLOT_X, GEAR_SLOT_Y));
        CuriosCompat.addSlots(extra, femboy, EXTRA_SLOT_X, CURIOS_SLOT_Y);
        return extra;
    }

    static IStorageWrapper clientWrapper(ItemStack chest) {
        return new FemboyChestStorageWrapper(chest);
    }

    static IStorageWrapper serverWrapper(FemboyEntity femboy) {
        return getOrCreate(femboy).wrapper;
    }

    private static Holder getOrCreate(FemboyEntity femboy) {
        return HOLDERS.computeIfAbsent(femboy, unused -> {
            ItemStack stack = createBasicChest();
            Holder holder = new Holder(stack, new FemboyChestStorageWrapper(stack));
            migrateVanillaItems(femboy, holder);
            return holder;
        });
    }

    private static void migrateVanillaItems(FemboyEntity femboy, Holder holder) {
        SimpleContainer vanilla = femboy.getInventory();
        InventoryHandler inv = holder.wrapper.getInventoryHandler();
        boolean movedAny = false;
        for (int i = 0; i < vanilla.getContainerSize(); i++) {
            ItemStack stack = vanilla.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = InventoryHelper.insertIntoInventory(stack, inv, false);
            vanilla.setItem(i, ItemStack.EMPTY);
            movedAny = true;
            if (!leftover.isEmpty()) {
                femboy.spawnAtLocation(leftover);
            }
        }
        if (movedAny) {
            femboy.playSound(SoundEvents.WOOD_PLACE, 0.6F, 1.2F);
        }
    }

    private static ItemStack createBasicChest() {
        Item item = ForgeRegistries.ITEMS.getValue(BASIC_CHEST_ID);
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
        if (!stack.isEmpty()) {
            WoodStorageBlockItem.setWoodType(stack, WoodType.OAK);
        }
        return stack;
    }

    private static boolean tryTierUpgrade(FemboyEntity femboy, Player player, ItemStack upgradeStack, StorageTierUpgradeItem upgradeItem) {
        Holder holder = getOrCreate(femboy);
        String currentId = ForgeRegistries.ITEMS.getKey(holder.stack.getItem()).toString();
        String nextId = nextChestId(upgradeItem.getTier(), currentId);
        if (nextId == null) {
            return false;
        }
        Item nextItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nextId));
        if (nextItem == null) {
            return false;
        }
        int oldInv = holder.wrapper.getNumberOfInventorySlots();
        int oldUpg = holder.wrapper.getUpgradeHandler().getSlots();
        ItemStack nextStack = new ItemStack(nextItem);
        if (holder.stack.hasTag()) {
            nextStack.setTag(holder.stack.getTag().copy());
        }
        if (nextItem instanceof WoodStorageBlockItem && WoodStorageBlockItem.getWoodType(holder.stack).isPresent()) {
            WoodStorageBlockItem.setWoodType(nextStack, WoodStorageBlockItem.getWoodType(holder.stack).orElse(WoodType.OAK));
        }
        FemboyChestStorageWrapper nextWrapper = new FemboyChestStorageWrapper(nextStack);
        int invDiff = defaultInventorySlots(nextStack) - oldInv;
        int upgDiff = defaultUpgradeSlots(nextStack) - oldUpg;
        nextWrapper.changeSize(invDiff, upgDiff);
        HOLDERS.put(femboy, new Holder(nextStack, nextWrapper));
        if (!player.getAbilities().instabuild) {
            upgradeStack.shrink(1);
        }
        femboy.playSound(SoundEvents.SMITHING_TABLE_USE, 1.0F, 1.0F);
        return true;
    }

    private static boolean tryInsertUpgrade(FemboyEntity femboy, Player player, ItemStack stack) {
        Holder holder = getOrCreate(femboy);
        UpgradeHandler upgrades = holder.wrapper.getUpgradeHandler();
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (!upgrades.getStackInSlot(i).isEmpty() || !upgrades.isItemValid(i, stack)) {
                continue;
            }
            ItemStack leftover = upgrades.insertItem(i, stack.copyWithCount(1), false);
            if (leftover.isEmpty()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                femboy.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static int defaultInventorySlots(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IStorageBlock storageBlock) {
                return storageBlock.getNumberOfInventorySlots();
            }
        }
        return 27;
    }

    private static int defaultUpgradeSlots(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IStorageBlock storageBlock) {
                return storageBlock.getNumberOfUpgradeSlots();
            }
        }
        return 1;
    }

    @Nullable
    private static String nextChestId(StorageTierUpgradeItem.TierUpgrade tier, String currentId) {
        return switch (tier) {
            case BASIC_TO_COPPER -> eq(currentId, "chest") ? "sophisticatedstorage:copper_chest" : null;
            case BASIC_TO_IRON -> eq(currentId, "chest") ? "sophisticatedstorage:iron_chest" : null;
            case BASIC_TO_GOLD -> eq(currentId, "chest") ? "sophisticatedstorage:gold_chest" : null;
            case BASIC_TO_DIAMOND -> eq(currentId, "chest") ? "sophisticatedstorage:diamond_chest" : null;
            case BASIC_TO_NETHERITE -> eq(currentId, "chest") ? "sophisticatedstorage:netherite_chest" : null;
            case COPPER_TO_IRON -> eq(currentId, "copper_chest") ? "sophisticatedstorage:iron_chest" : null;
            case COPPER_TO_GOLD -> eq(currentId, "copper_chest") ? "sophisticatedstorage:gold_chest" : null;
            case COPPER_TO_DIAMOND -> eq(currentId, "copper_chest") ? "sophisticatedstorage:diamond_chest" : null;
            case COPPER_TO_NETHERITE -> eq(currentId, "copper_chest") ? "sophisticatedstorage:netherite_chest" : null;
            case IRON_TO_GOLD -> eq(currentId, "iron_chest") ? "sophisticatedstorage:gold_chest" : null;
            case IRON_TO_DIAMOND -> eq(currentId, "iron_chest") ? "sophisticatedstorage:diamond_chest" : null;
            case IRON_TO_NETHERITE -> eq(currentId, "iron_chest") ? "sophisticatedstorage:netherite_chest" : null;
            case GOLD_TO_DIAMOND -> eq(currentId, "gold_chest") ? "sophisticatedstorage:diamond_chest" : null;
            case GOLD_TO_NETHERITE -> eq(currentId, "gold_chest") ? "sophisticatedstorage:netherite_chest" : null;
            case DIAMOND_TO_NETHERITE -> eq(currentId, "diamond_chest") ? "sophisticatedstorage:netherite_chest" : null;
            default -> null;
        };
    }

    private static boolean eq(String currentId, String path) {
        return ("sophisticatedstorage:" + path).equals(currentId);
    }

    private static void dropTierUpgradeRefund(FemboyEntity femboy, ItemStack chest) {
        ResourceLocation chestId = ForgeRegistries.ITEMS.getKey(chest.getItem());
        if (chestId == null || !"sophisticatedstorage".equals(chestId.getNamespace())) {
            return;
        }
        String upgradePath = switch (chestId.getPath()) {
            case "copper_chest" -> "basic_to_copper_tier_upgrade";
            case "iron_chest" -> "basic_to_iron_tier_upgrade";
            case "gold_chest" -> "basic_to_gold_tier_upgrade";
            case "diamond_chest" -> "basic_to_diamond_tier_upgrade";
            case "netherite_chest" -> "basic_to_netherite_tier_upgrade";
            default -> null;
        };
        if (upgradePath == null) {
            return;
        }
        Item upgrade = ForgeRegistries.ITEMS.getValue(new ResourceLocation("sophisticatedstorage", upgradePath));
        if (upgrade != null) {
            femboy.spawnAtLocation(new ItemStack(upgrade));
        }
    }

    static boolean isOpenFor(Player player, FemboyEntity femboy) {
        if (player.containerMenu instanceof FemboyStorageMenu menu) {
            return menu.getFemboy() == femboy;
        }
        if (player.containerMenu instanceof FemboyStorageSettingsMenu menu) {
            return menu.getFemboy() == femboy;
        }
        return false;
    }

    private record Holder(ItemStack stack, FemboyChestStorageWrapper wrapper) {}
}
