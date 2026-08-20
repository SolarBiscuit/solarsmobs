package com.solarbiscuit.inventory.femboy;

import com.solarbiscuit.entity.femboy.FemboyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FemboyEquipmentContainer extends SimpleContainer {
    private final FemboyEntity femboy;

    public FemboyEquipmentContainer(FemboyEntity femboy) {
        super(6);
        this.femboy = femboy;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.femboy.setItemSlot(EquipmentSlot.values()[slot], stack);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.femboy.getItemBySlot(EquipmentSlot.values()[slot]);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty()) {
            setItem(slot, ItemStack.EMPTY);
        }
        return stack;
    }

    @Override
    public void setChanged() {
    }

    public static boolean mayPlaceIn(EquipmentSlot slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return Mob.getEquipmentSlotForItem(stack) == slot;
        }
        if (slot == EquipmentSlot.OFFHAND) {
            return isShield(stack) || isHandheldLight(stack);
        }
        return true;
    }

    public static boolean isShield(ItemStack stack) {
        return stack.is(Items.SHIELD) || stack.canPerformAction(ToolActions.SHIELD_BLOCK);
    }

    public static boolean isHandheldLight(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String path = id != null ? id.getPath() : "";
        String hay = (path + " " + stack.getDescriptionId() + " " + stack.getHoverName().getString())
                .toLowerCase(Locale.ROOT);
        return hay.contains("torch") || hay.contains("lantern") || hay.contains("light");
    }

    public static List<Slot> stackedSlots(FemboyEntity femboy, int x, int startY) {
        FemboyEquipmentContainer wrapper = new FemboyEquipmentContainer(femboy);
        List<Slot> slots = new ArrayList<>(6);
        slots.add(new GearSlot(wrapper, 5, x, startY, EquipmentSlot.HEAD));
        slots.add(new GearSlot(wrapper, 4, x, startY + 18, EquipmentSlot.CHEST));
        slots.add(new GearSlot(wrapper, 3, x, startY + 36, EquipmentSlot.LEGS));
        slots.add(new GearSlot(wrapper, 2, x, startY + 54, EquipmentSlot.FEET));
        slots.add(new GearSlot(wrapper, 0, x, startY + 72, EquipmentSlot.MAINHAND));
        slots.add(new GearSlot(wrapper, 1, x, startY + 90, EquipmentSlot.OFFHAND));
        return slots;
    }

    public static final class GearSlot extends Slot {
        private final EquipmentSlot equipmentSlot;

        public GearSlot(FemboyEquipmentContainer container, int index, int x, int y, EquipmentSlot equipmentSlot) {
            super(container, index, x, y);
            this.equipmentSlot = equipmentSlot;
            ResourceLocation icon = emptyIcon(equipmentSlot);
            if (icon != null) {
                this.setBackground(InventoryMenu.BLOCK_ATLAS, icon);
            }
        }

        public EquipmentSlot getEquipmentSlot() {
            return this.equipmentSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FemboyEquipmentContainer.mayPlaceIn(this.equipmentSlot, stack);
        }

        @Override
        public int getMaxStackSize() {
            return this.equipmentSlot.getType() == EquipmentSlot.Type.HAND ? 64 : 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            if (this.equipmentSlot == EquipmentSlot.MAINHAND) {
                return Math.min(64, stack.getMaxStackSize());
            }
            if (this.equipmentSlot == EquipmentSlot.OFFHAND) {
                return isShield(stack) ? 1 : Math.min(64, stack.getMaxStackSize());
            }
            return 1;
        }
    }

    private static ResourceLocation emptyIcon(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
            case OFFHAND -> InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            default -> null;
        };
    }
}
