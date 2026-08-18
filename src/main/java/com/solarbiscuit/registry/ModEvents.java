package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final int ABSORPTION_TICKS = 1200;
    private static final int BRAVEHEART_WINDOW = 600;

    @SubscribeEvent
    public static void onDrinkFromModdedBucket(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        var stack = event.getItemStack();

        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            if (handler.getFluidInTank(0).getFluid() == ModFluids.FEMBOY_MILK.get() && handler.getFluidInTank(0).getAmount() >= 1000) {
                if (!player.level().isClientSide()) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS, 0));
                    player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));

                    if (!player.isCreative()) {
                        handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                        player.setItemInHand(event.getHand(), handler.getContainer());
                    }
                }

                player.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }

        if (FactionRelations.hasSacredEnderRing(player) && player.level().dimension() == Level.END) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, true));
        }

        AABB nearby = player.getBoundingBox().inflate(5.0D);
        boolean sawModMob = false;
        boolean nearThiefWithNecklace = false;
        boolean nearTemplarWithCross = false;
        boolean nearEndWarrior = false;

        for (Entity entity : player.level().getEntities(player, nearby, ModEvents::isSolarsMob)) {
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

    private static boolean isEndRelated(LivingEntity entity) {
        if (entity instanceof EnderMan || entity instanceof EnderDragon || entity instanceof Endermite || entity instanceof Shulker) {
            return true;
        }
        return entity instanceof Factioned factioned && factioned.getFaction() == Faction.ENDER;
    }

    private static boolean isSolarsMob(Entity entity) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && SolarsMobs.MOD_ID.equals(key.getNamespace()) && entity.getType() != EntityType.PLAYER;
    }
}
