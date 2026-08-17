package com.solarbiscuit.loot;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModLootHandler {
    private static final String SIMPLE_HATS_ID = "simplehats";
    private static final ResourceLocation SHARED_MOB_LOOT =
            new ResourceLocation(SolarsMobs.MOD_ID, "entities/shared");
    private static final ResourceLocation SIMPLE_DUNGEON =
            new ResourceLocation("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation HATBAG_COMMON =
            new ResourceLocation(SIMPLE_HATS_ID, "hatbag_common");

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();

        if (SIMPLE_DUNGEON.equals(id)) {
            event.getTable().addPool(LootPool.lootPool()
                    .name("solarsmobs_thieves_guild_necklace")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.THIEVES_GUILD_NECKLACE.get())
                            .when(LootItemRandomChanceCondition.randomChance(0.001F)))
                    .build());
        }

        if (SHARED_MOB_LOOT.equals(id)) {
            addSimplyHatsPool(event);
        }
    }

    private static void addSimplyHatsPool(LootTableLoadEvent event) {
        if (!ModList.get().isLoaded(SIMPLE_HATS_ID)) {
            return;
        }
        Item hatbag = ForgeRegistries.ITEMS.getValue(HATBAG_COMMON);
        if (hatbag == null || hatbag == Items.AIR) {
            return;
        }
        event.getTable().addPool(LootPool.lootPool()
                .name("solarsmobs_simplyhats")
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(hatbag)
                        .when(LootItemRandomChanceCondition.randomChance(0.02F)))
                .build());
    }
}
