package com.solarbiscuit.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class MobEntry<T extends LivingEntity> {
    private final RegistryObject<EntityType<T>> type;
    private final Supplier<AttributeSupplier> attributes;
    private final SpawnPlacements.Type spawnType;
    private final Heightmap.Types heightmap;
    private final SpawnPlacements.SpawnPredicate<T> spawnPredicate;
    private final Supplier<RegistryObject<Item>> spawnEgg;

    public MobEntry(
            RegistryObject<EntityType<T>> type,
            Supplier<AttributeSupplier> attributes,
            SpawnPlacements.Type spawnType,
            Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> spawnPredicate,
            Supplier<RegistryObject<Item>> spawnEgg
    ) {
        this.type = type;
        this.attributes = attributes;
        this.spawnType = spawnType;
        this.heightmap = heightmap;
        this.spawnPredicate = spawnPredicate;
        this.spawnEgg = spawnEgg;
    }

    public RegistryObject<EntityType<T>> type() {
        return type;
    }

    public RegistryObject<Item> spawnEgg() {
        return spawnEgg.get();
    }

    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(type.get(), attributes.get());
    }

    public void registerSpawn(SpawnPlacementRegisterEvent event) {
        event.register(type.get(), spawnType, heightmap, spawnPredicate, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
