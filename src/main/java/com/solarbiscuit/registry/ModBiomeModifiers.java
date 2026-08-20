package com.solarbiscuit.registry;

import com.mojang.serialization.Codec;
import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.world.ForestNameSpawnBiomeModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SolarsMobs.MOD_ID);

    public static final RegistryObject<Codec<ForestNameSpawnBiomeModifier>> FOREST_NAME_SPAWNS =
            SERIALIZERS.register("forest_name_spawns", () -> ForestNameSpawnBiomeModifier.CODEC);

    private ModBiomeModifiers() {}
}
