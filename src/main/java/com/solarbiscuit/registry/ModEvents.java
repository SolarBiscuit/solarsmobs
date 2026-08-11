package com.solarbiscuit.registry;

import com.solarbiscuit.WildFemboys;
import com.solarbiscuit.registry.ModFluids;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fluids.capability.IFluidHandler;

@Mod.EventBusSubscriber(modid = WildFemboys.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    // Allows instant drinking from any modded fluid container via standard right-click
    @SubscribeEvent
    public static void onDrinkFromModdedBucket(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        var stack = event.getItemStack();

        // Check if the item in hand can hold fluids (like MoreBuckets)
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            
            // Verify there is at least 1 full bucket (1000mB) of our custom milk inside
            if (handler.getFluidInTank(0).getFluid() == ModFluids.FEMBOY_MILK.get() && handler.getFluidInTank(0).getAmount() >= 1000) {
                
                if (!player.level().isClientSide()) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1800, 0));
                    player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
                    
                    if (!player.isCreative()) {
                        handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                        // Ensures the MoreBucket remembers it has been emptied
                        player.setItemInHand(event.getHand(), handler.getContainer());
                    }
                }
                
                // Play sound and safely cancel the vanilla right-click so we don't accidentally place a block
                player.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        });
    }
}