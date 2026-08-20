package com.solarbiscuit.event;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnderRingEvents {
    public static final String ENDERMAN_OVERHAUL_ID = "endermanoverhaul";
    private static final int BRAVEHEART_WINDOW = 600;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (FactionRelations.hasSacredEnderRing(player)) {
            calmEndermen(player);
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (FactionRelations.hasSacredEnderRing(player) && player.level().dimension() == Level.END) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, true));
        }
    }

    @SubscribeEvent
    public static void onEndermanAnger(EnderManAngerEvent event) {
        if (FactionRelations.hasSacredEnderRing(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!isEndRelated(victim)) {
            return;
        }
        AABB box = victim.getBoundingBox().inflate(48.0D);
        for (EndWarriorEntity warrior : victim.level().getEntitiesOfClass(EndWarriorEntity.class, box)) {
            if (warrior != victim) {
                warrior.angerFromPack(player);
            }
        }
        if (victim instanceof EndWarriorEntity hitWarrior) {
            hitWarrior.angerFromPack(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!isEndRelated(victim)) {
            return;
        }

        CompoundTag persisted = player.getPersistentData().getCompound(ServerPlayer.PERSISTED_NBT_TAG);
        boolean primed = persisted.getBoolean("solarsmobs_ender_primed");
        long primeTime = persisted.getLong("solarsmobs_ender_prime_time");
        long now = player.level().getGameTime();
        boolean chargingWarrior = victim instanceof EndWarriorEntity warrior && warrior.isCharging();

        if (chargingWarrior && primed && now - primeTime <= BRAVEHEART_WINDOW) {
            int count = persisted.getInt("solarsmobs_braveheart_count") + 1;
            persisted.putInt("solarsmobs_braveheart_count", count);
            if (count >= 3) {
                ModAdvancements.award(player, ModAdvancements.BRAVEHEART, "killed_charging");
            }
        } else {
            persisted.putBoolean("solarsmobs_ender_primed", true);
            persisted.putLong("solarsmobs_ender_prime_time", now);
            persisted.putInt("solarsmobs_braveheart_count", 0);
        }

        player.getPersistentData().put(ServerPlayer.PERSISTED_NBT_TAG, persisted);
    }

    private static void calmEndermen(Player player) {
        AABB box = player.getBoundingBox().inflate(48.0D);
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, box, EnderRingEvents::isEndermanLike)) {
            boolean targetingPlayer = mob.getTarget() == player
                    || mob.getLastHurtByMob() == player
                    || (mob instanceof NeutralMob neutral
                    && player.getUUID().equals(neutral.getPersistentAngerTarget()));
            if (!targetingPlayer) {
                continue;
            }
            if (mob instanceof NeutralMob neutral) {
                neutral.stopBeingAngry();
            }
            if (mob.getTarget() == player) {
                mob.setTarget(null);
            }
            if (mob.getLastHurtByMob() == player) {
                mob.setLastHurtByMob(null);
            }
        }
    }

    public static boolean isEndermanLike(LivingEntity entity) {
        if (entity instanceof EnderMan) {
            return true;
        }
        var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && ENDERMAN_OVERHAUL_ID.equals(key.getNamespace());
    }

    private static boolean isEndRelated(LivingEntity entity) {
        if (entity instanceof EnderMan || entity instanceof EnderDragon || entity instanceof Endermite || entity instanceof Shulker) {
            return true;
        }
        if (isEndermanLike(entity)) {
            return true;
        }
        return entity instanceof Factioned factioned && factioned.getFaction() == Faction.ENDER;
    }
}
