package com.solarbiscuit.item.femboy;

import com.solarbiscuit.registry.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemUtils;

public class FemboyMilkBucketItem extends BucketItem {
    
    public FemboyMilkBucketItem(Properties properties) {
        // Links this bucket to the fluid we created
        super(ModFluids.FEMBOY_MILK, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 32; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (!level.isClientSide) {
            // Vanilla milk wipe first, then Absorption I for 60 seconds (1200 ticks)
            entityLiving.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
            entityLiving.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0));
        }
        
        if (entityLiving instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(Items.BUCKET); // Returns empty bucket!
            }
            player.getInventory().add(new ItemStack(Items.BUCKET));
        }
        return stack;
    }
}