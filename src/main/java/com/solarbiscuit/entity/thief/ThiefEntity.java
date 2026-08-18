package com.solarbiscuit.entity.thief;

import com.solarbiscuit.entity.EquipmentDrops;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ThiefEntity extends Monster implements Factioned {
    public static final int MAX_SKINS = 9;
    public static final double STALK_RANGE = 12.0D;

    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(ThiefEntity.class, EntityDataSerializers.INT);

    public ThiefEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return Faction.EVIL;
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
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StalkPlayerGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                living -> living instanceof Player player && FactionRelations.thiefShouldHuntPlayer(player)));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean sneaking = shouldSneak();
        this.setShiftKeyDown(sneaking);
        this.setPose(sneaking ? Pose.CROUCHING : Pose.STANDING);
    }

    private boolean shouldSneak() {
        LivingEntity target = this.getTarget();
        if (target instanceof Player player && isPlayerFacing(player, this) && this.distanceTo(player) <= STALK_RANGE) {
            return false;
        }
        return true;
    }

    public static boolean isPlayerFacing(Player player, LivingEntity target) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 toTarget = target.position().add(0.0D, target.getEyeHeight() * 0.5D, 0.0D).subtract(player.getEyePosition()).normalize();
        return look.dot(toTarget) > 0.35D;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        RandomSource random = this.getRandom();
        setSkinIndex(random.nextInt(MAX_SKINS));
        equipRandomChainmail(random);
        equipRandomWeapon(random);
        EquipmentDrops.disableAll(this);
        return data;
    }

    private void equipRandomChainmail(RandomSource random) {
        maybeEquip(EquipmentSlot.HEAD, Items.CHAINMAIL_HELMET, random);
        maybeEquip(EquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE, random);
        maybeEquip(EquipmentSlot.LEGS, Items.CHAINMAIL_LEGGINGS, random);
        maybeEquip(EquipmentSlot.FEET, Items.CHAINMAIL_BOOTS, random);
    }

    private void equipRandomWeapon(RandomSource random) {
        Item weapon = switch (random.nextInt(3)) {
            case 0 -> Items.WOODEN_SWORD;
            case 1 -> Items.STONE_SWORD;
            default -> Items.STONE_AXE;
        };
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
    }

    private void maybeEquip(EquipmentSlot slot, Item item, RandomSource random) {
        if (random.nextBoolean()) {
            this.setItemSlot(slot, new ItemStack(item));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinIndex", getSkinIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinIndex")) {
            setSkinIndex(tag.getInt("SkinIndex"));
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player player && !FactionRelations.thiefShouldHuntPlayer(player)
                && this.getLastHurtByMob() != player) {
            return false;
        }
        return super.canAttack(target);
    }

    private static class StalkPlayerGoal extends Goal {
        private final ThiefEntity thief;
        private Player prey;
        private int recalcPath;

        StalkPlayerGoal(ThiefEntity thief) {
            this.thief = thief;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player closest = this.thief.level().getNearestPlayer(this.thief, STALK_RANGE);
            if (closest == null || !closest.isAlive() || closest.isSpectator() || closest.isCreative()) {
                return false;
            }
            if (!FactionRelations.thiefShouldHuntPlayer(closest) && this.thief.getLastHurtByMob() != closest) {
                return false;
            }
            if (ThiefEntity.isPlayerFacing(closest, this.thief)) {
                return false;
            }
            this.prey = closest;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.prey != null
                    && this.prey.isAlive()
                    && this.thief.distanceTo(this.prey) <= STALK_RANGE + 2.0D
                    && !ThiefEntity.isPlayerFacing(this.prey, this.thief);
        }

        @Override
        public void start() {
            this.recalcPath = 0;
            this.thief.setTarget(this.prey);
            this.thief.setShiftKeyDown(true);
        }

        @Override
        public void stop() {
            this.prey = null;
            this.thief.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.prey == null) {
                return;
            }
            this.thief.getLookControl().setLookAt(this.prey, 30.0F, 30.0F);
            if (--this.recalcPath <= 0) {
                this.recalcPath = 8;
                this.thief.getNavigation().moveTo(this.prey, 0.85D);
            }
        }
    }
}
