package com.solarbiscuit.entity.endwarrior;

import com.solarbiscuit.entity.EquipmentDrops;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EndWarriorEntity extends PathfinderMob implements NeutralMob, Factioned {
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final UUID DRAGON_AURA_ID = UUID.fromString("6c4d2b1a-9e70-4d3f-8a11-b7e4c0f02d55");
    private static final AttributeModifier DRAGON_AURA = new AttributeModifier(
            DRAGON_AURA_ID, "Ender dragon aura", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public EndWarriorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return Faction.ENDER;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.updatePersistentAnger(serverLevel, true);
            updateDragonAura();
        }
    }

    public boolean isCharging() {
        return this.getTarget() instanceof Player || this.isAngry();
    }

    public void angerFromPack(Player player) {
        this.setPersistentAngerTarget(player.getUUID());
        this.startPersistentAngerTimer();
        this.setTarget(player);
        this.setLastHurtByMob(player);
    }

    private void updateDragonAura() {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        boolean dragonNearby = !this.level().getEntitiesOfClass(EnderDragon.class, this.getBoundingBox().inflate(128.0D),
                EnderDragon::isAlive).isEmpty();
        boolean hasAura = health.getModifier(DRAGON_AURA_ID) != null;
        if (dragonNearby && !hasAura) {
            health.addPermanentModifier(DRAGON_AURA);
            this.setHealth(this.getHealth() * 2.0F);
        } else if (!dragonNearby && hasAura) {
            float ratio = this.getHealth() / this.getMaxHealth();
            health.removeModifier(DRAGON_AURA_ID);
            this.setHealth(Math.max(1.0F, this.getMaxHealth() * ratio));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.NETHERITE_SWORD));
        EquipmentDrops.disableAll(this);
        return data;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isAngry();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }
}
