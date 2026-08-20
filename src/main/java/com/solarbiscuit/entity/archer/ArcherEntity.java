package com.solarbiscuit.entity.archer;

import com.solarbiscuit.entity.EquipmentDrops;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class ArcherEntity extends Monster implements NeutralMob, Factioned, RangedAttackMob, CrossbowAttackMob {
    public static final int MAX_SKINS = 4;
    public static final float SHOT_RANGE = 12.0F;
    public static final float SPRINT_RANGE = 6.0F;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(ArcherEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_CROSSBOW =
            SynchedEntityData.defineId(ArcherEntity.class, EntityDataSerializers.BOOLEAN);

    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private boolean pendingShotAfterKite;

    public ArcherEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return Faction.NEUTRAL;
    }

    public int getSkinIndex() {
        return this.entityData.get(DATA_SKIN);
    }

    public void setSkinIndex(int index) {
        this.entityData.set(DATA_SKIN, Math.floorMod(index, MAX_SKINS));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN, 0);
        this.entityData.define(DATA_CHARGING_CROSSBOW, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SprintAwayGoal(this));
        this.goalSelector.addGoal(2, new GuaranteedKiteShotGoal(this));
        this.goalSelector.addGoal(3, new RangedBowAttackGoal<>(this, 1.0D, 10, SHOT_RANGE));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0D, SHOT_RANGE));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                FactionRelations::isMonsterFoe));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.updatePersistentAnger(serverLevel, true);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        setSkinIndex(this.random.nextInt(MAX_SKINS));
        ItemStack weapon = this.random.nextBoolean() ? new ItemStack(Items.BOW) : new ItemStack(Items.CROSSBOW);
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        EquipmentDrops.disableAll(this);
        return data;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof Player player && this.isAlive()) {
            this.setPersistentAngerTarget(player.getUUID());
            this.startPersistentAngerTimer();
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
        return weapon instanceof BowItem || weapon instanceof CrossbowItem;
    }

    @Override
    public ItemStack getProjectile(ItemStack weapon) {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.isHolding(stack -> stack.getItem() instanceof CrossbowItem)) {
            ItemStack crossbow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof CrossbowItem));
            if (!CrossbowItem.isCharged(crossbow)) {
                CrossbowItem.setCharged(crossbow, true);
            }
            this.performCrossbowAttack(this, 1.6F);
            this.pendingShotAfterKite = false;
            return;
        }
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        float power = Math.max(velocity, 1.0F);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, this.getProjectile(bow), power);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + dist * 0.2D, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
        this.pendingShotAfterKite = false;
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float angle) {
        this.shootCrossbowProjectile(this, target, projectile, angle, 1.6F);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
        this.entityData.set(DATA_CHARGING_CROSSBOW, charging);
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(DATA_CHARGING_CROSSBOW);
    }

    public boolean isChargingShot() {
        return this.isUsingItem() || this.isChargingCrossbow();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof ArcherEntity) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
        tag.putInt("SkinIndex", getSkinIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
        if (tag.contains("SkinIndex")) {
            setSkinIndex(tag.getInt("SkinIndex"));
        }
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

    private static class SprintAwayGoal extends Goal {
        private final ArcherEntity archer;
        private int recalc;

        SprintAwayGoal(ArcherEntity archer) {
            this.archer = archer;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.archer.isChargingShot()) {
                return false;
            }
            LivingEntity target = this.archer.getTarget();
            return target != null && target.isAlive() && this.archer.distanceTo(target) <= SPRINT_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.archer.isChargingShot()) {
                return false;
            }
            LivingEntity target = this.archer.getTarget();
            return target != null && target.isAlive() && this.archer.distanceTo(target) <= SPRINT_RANGE;
        }

        @Override
        public void start() {
            this.archer.setSprinting(true);
            this.recalc = 0;
        }

        @Override
        public void stop() {
            this.archer.setSprinting(false);
            this.archer.getNavigation().stop();
            LivingEntity target = this.archer.getTarget();
            if (target != null && target.isAlive() && this.archer.distanceTo(target) > SPRINT_RANGE) {
                this.archer.pendingShotAfterKite = true;
            }
        }

        @Override
        public void tick() {
            LivingEntity target = this.archer.getTarget();
            if (target == null) {
                return;
            }
            this.archer.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.recalc <= 0) {
                this.recalc = 5;
                double dx = this.archer.getX() - target.getX();
                double dz = this.archer.getZ() - target.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < 0.001D) {
                    dx = this.archer.getRandom().nextGaussian();
                    dz = this.archer.getRandom().nextGaussian();
                    dist = Math.sqrt(dx * dx + dz * dz);
                }
                double scale = 8.0D / dist;
                this.archer.getNavigation().moveTo(
                        this.archer.getX() + dx * scale,
                        this.archer.getY(),
                        this.archer.getZ() + dz * scale,
                        1.45D);
            }
        }
    }

    private static class GuaranteedKiteShotGoal extends Goal {
        private final ArcherEntity archer;
        private int chargeTicks;

        GuaranteedKiteShotGoal(ArcherEntity archer) {
            this.archer = archer;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.archer.getTarget();
            return this.archer.pendingShotAfterKite
                    && target != null
                    && target.isAlive()
                    && this.archer.distanceTo(target) > SPRINT_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.archer.getTarget();
            if (!this.archer.pendingShotAfterKite || target == null || !target.isAlive()) {
                return false;
            }
            if (this.chargeTicks > 0 || this.archer.isChargingShot()) {
                return true;
            }
            return this.archer.distanceTo(target) > SPRINT_RANGE;
        }

        @Override
        public void start() {
            this.chargeTicks = 0;
            this.archer.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.archer.getTarget();
            if (target == null) {
                return;
            }
            this.archer.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double dist = this.archer.distanceTo(target);
            if (dist > SHOT_RANGE) {
                this.archer.getNavigation().moveTo(target, 1.0D);
                return;
            }
            this.archer.getNavigation().stop();
            if (!this.archer.isUsingItem() && this.archer.isHolding(stack -> stack.getItem() instanceof BowItem)) {
                this.archer.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.archer, item -> item instanceof BowItem));
            }
            this.chargeTicks++;
            if (this.chargeTicks >= 8) {
                this.archer.performRangedAttack(target, 1.0F);
                this.archer.stopUsingItem();
            }
        }
    }
}
