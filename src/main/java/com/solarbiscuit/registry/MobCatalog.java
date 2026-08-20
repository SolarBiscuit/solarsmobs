package com.solarbiscuit.registry;

import com.solarbiscuit.entity.archer.ArcherEntity;
import com.solarbiscuit.entity.arborist.ArboristEntity;
import com.solarbiscuit.entity.endwarrior.EndWarriorEntity;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.templar.TemplarEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public final class MobCatalog {
    public static final MobEntry<FemboyEntity> FEMBOY = new MobEntry<>(
            ModEntities.FEMBOY,
            () -> FemboyEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Animal::checkAnimalSpawnRules,
            () -> ModItems.FEMBOY_SPAWN_EGG
    );

    public static final MobEntry<ThiefEntity> THIEF = new MobEntry<>(
            ModEntities.THIEF,
            () -> ThiefEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules,
            () -> ModItems.THIEF_SPAWN_EGG
    );

    public static final MobEntry<TemplarEntity> TEMPLAR = new MobEntry<>(
            ModEntities.TEMPLAR,
            () -> TemplarEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Mob::checkMobSpawnRules,
            () -> ModItems.TEMPLAR_SPAWN_EGG
    );

    public static final MobEntry<EndWarriorEntity> END_WARRIOR = new MobEntry<>(
            ModEntities.END_WARRIOR,
            () -> EndWarriorEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, level, spawnType, pos, random) ->
                    level.getDifficulty() != Difficulty.PEACEFUL
                            && Mob.checkMobSpawnRules(type, level, spawnType, pos, random),
            () -> ModItems.END_WARRIOR_SPAWN_EGG
    );

    public static final MobEntry<ArcherEntity> ARCHER = new MobEntry<>(
            ModEntities.ARCHER,
            () -> ArcherEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Mob::checkMobSpawnRules,
            () -> ModItems.ARCHER_SPAWN_EGG
    );

    public static final MobEntry<ArboristEntity> ARBORIST = new MobEntry<>(
            ModEntities.ARBORIST,
            () -> ArboristEntity.createAttributes().build(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Mob::checkMobSpawnRules,
            () -> ModItems.ARBORIST_SPAWN_EGG
    );

    public static final List<MobEntry<?>> ALL = List.of(FEMBOY, THIEF, TEMPLAR, END_WARRIOR, ARCHER, ARBORIST);

    private MobCatalog() {}
}
