package com.solarbiscuit.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.Locale;

/**
 * Extra spawns in overworld biomes tagged as forest or whose registry path contains a substring
 * (so Biomes O' Plenty / Atmospheric / etc. forests are included without a hardcoded list).
 */
public record ForestNameSpawnBiomeModifier(
        String contains,
        boolean overworldOnly,
        MobCategory category,
        List<MobSpawnSettings.SpawnerData> spawners
) implements BiomeModifier {

    public static final Codec<ForestNameSpawnBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("contains", "forest").forGetter(ForestNameSpawnBiomeModifier::contains),
            Codec.BOOL.optionalFieldOf("overworld_only", true).forGetter(ForestNameSpawnBiomeModifier::overworldOnly),
            MobCategory.CODEC.fieldOf("category").forGetter(ForestNameSpawnBiomeModifier::category),
            MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(ForestNameSpawnBiomeModifier::spawners)
    ).apply(instance, ForestNameSpawnBiomeModifier::new));

    private static final TagKey<Biome> FORGE_IS_FOREST = TagKey.create(Registries.BIOME, new ResourceLocation("forge", "is_forest"));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || this.spawners.isEmpty()) {
            return;
        }
        if (this.overworldOnly && isNetherOrEnd(biome)) {
            return;
        }
        if (!isForestLike(biome)) {
            return;
        }
        for (MobSpawnSettings.SpawnerData spawner : this.spawners) {
            builder.getMobSpawnSettings().addSpawn(this.category, spawner);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }

    private boolean isForestLike(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(FORGE_IS_FOREST)) {
            return true;
        }
        ResourceLocation id = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id == null) {
            return false;
        }
        String needle = this.contains.toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (path.contains(needle)) {
            return true;
        }
        String strippedPath = path.replace("_", "").replace("-", "");
        String strippedNeedle = needle.replace("_", "").replace("-", "");
        return strippedPath.contains(strippedNeedle);
    }

    private static boolean isNetherOrEnd(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END);
    }
}
