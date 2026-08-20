package com.solarbiscuit.event;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import com.solarbiscuit.faction.FactionRelations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdvancementEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }

        AABB nearby = player.getBoundingBox().inflate(5.0D);
        boolean sawModMob = false;
        boolean nearThiefWithNecklace = false;
        boolean nearTemplarWithCross = false;
        boolean nearEndWarrior = false;

        for (Entity entity : player.level().getEntities(player, nearby, AdvancementEvents::isSolarsMob)) {
            double distance = player.distanceTo(entity);
            if (distance <= 4.0D) {
                sawModMob = true;
            }
            if (entity instanceof ThiefEntity && distance <= 5.0D && FactionRelations.hasThievesGuildProtection(player)) {
                nearThiefWithNecklace = true;
            }
            if (entity instanceof TemplarEntity && distance <= 4.0D && FactionRelations.hasHolyCross(player)) {
                nearTemplarWithCross = true;
            }
            if (entity instanceof EndWarriorEntity && distance <= 4.0D) {
                nearEndWarrior = true;
            }
        }

        if (sawModMob) {
            ModAdvancements.award(player, ModAdvancements.WHOLE_NEW_WORLD, "near_mob");
        }
        if (nearThiefWithNecklace) {
            ModAdvancements.award(player, ModAdvancements.BROTHERHOOD, "near_thief");
        }
        if (nearTemplarWithCross) {
            ModAdvancements.award(player, ModAdvancements.DEFENDER_OF_THE_FAITH, "near_templar");
        }
        if (nearEndWarrior) {
            ModAdvancements.award(player, ModAdvancements.NEW_GUARD, "near_end_warrior");
        }
    }

    private static boolean isSolarsMob(Entity entity) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && SolarsMobs.MOD_ID.equals(key.getNamespace()) && entity.getType() != EntityType.PLAYER;
    }
}
