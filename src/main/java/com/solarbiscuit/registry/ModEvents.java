package com.solarbiscuit.registry;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import com.solarbiscuit.faction.FactionRelations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final int ABSORPTION_TICKS = 1200;

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

        AABB nearby = player.getBoundingBox().inflate(5.0D);
        boolean sawModMob = false;
        boolean nearThiefWithNecklace = false;
        boolean nearTemplarWithCross = false;

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
    }

    private static boolean isSolarsMob(Entity entity) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && SolarsMobs.MOD_ID.equals(key.getNamespace()) && entity.getType() != EntityType.PLAYER;
    }
}
