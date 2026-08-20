package com.solarbiscuit.event;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.registry.ModFluids;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MilkDrinkEvents {
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
}
