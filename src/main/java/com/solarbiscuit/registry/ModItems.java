package com.solarbiscuit.registry;

import com.solarbiscuit.WildFemboys;
import com.solarbiscuit.wildfemboys.item.FemboyMilkBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WildFemboys.MOD_ID);

	public static final RegistryObject<Item> FEMBOY_MILK_BUCKET = ITEMS.register("femboy_milk_bucket",
        () -> new FemboyMilkBucketItem(new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
}