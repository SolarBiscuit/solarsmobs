package com.solarbiscuit.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;

public final class EquipmentDrops {
    private EquipmentDrops() {}

    public static void disableAll(Mob mob) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0F);
        }
    }
}
