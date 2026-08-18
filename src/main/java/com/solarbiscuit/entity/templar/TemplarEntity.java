package com.solarbiscuit.entity.templar;

import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.compat.simplyswords.SimplySwordsCompat;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class TemplarEntity extends PathfinderMob implements NeutralMob, Factioned {
    public static final int HIRE_TICKS_PER_INGOT = 1800;

    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private int remainingHireTicks;
    @Nullable
    private UUID recruiterUUID;
    private int recruiterGoldGiven;

    public TemplarEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return Faction.HOLY;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new FollowRecruiterGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !TemplarEntity.this.isHired() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity attacker = TemplarEntity.this.getLastHurtByMob();
                return !TemplarEntity.this.isRecruiter(attacker) && super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new ProtectRecruiterGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                FactionRelations::isHolyFoe));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                this::isAngryAt));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
            if (this.remainingHireTicks > 0) {
                this.remainingHireTicks--;
                if (this.remainingHireTicks <= 0) {
                    this.setTarget(null);
                }
            }
        }
    }

    public boolean isHired() {
        return this.remainingHireTicks > 0 && this.recruiterUUID != null;
    }

    public boolean isRecruiter(@Nullable LivingEntity entity) {
        return entity != null && this.recruiterUUID != null && this.recruiterUUID.equals(entity.getUUID());
    }

    @Nullable
    public Player getRecruiter() {
        if (this.recruiterUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.recruiterUUID);
    }

    public int getRemainingHireTicks() {
        return this.remainingHireTicks;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GOLD_INGOT) && FactionRelations.hasHolyCross(player)) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (this.recruiterUUID != null && !this.recruiterUUID.equals(player.getUUID())) {
                    this.remainingHireTicks = 0;
                    this.recruiterGoldGiven = 0;
                }
                if (this.recruiterUUID != null && this.recruiterUUID.equals(player.getUUID())) {
                    this.recruiterGoldGiven++;
                } else {
                    this.recruiterGoldGiven = 1;
                }
                this.recruiterUUID = player.getUUID();
                this.remainingHireTicks += HIRE_TICKS_PER_INGOT;
                this.setPersistenceRequired();
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + 1.2D, this.getZ(),
                            8, 0.3D, 0.3D, 0.3D, 0.02D);
                }
                if (player instanceof ServerPlayer serverPlayer && this.recruiterGoldGiven >= 20) {
                    ModAdvancements.award(serverPlayer, ModAdvancements.TITHE, "gave_gold");
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.setItemSlot(EquipmentSlot.MAINHAND, SimplySwordsCompat.randomIronWeapon(this.getRandom()));
        this.setItemSlot(EquipmentSlot.OFFHAND, createBannerShield());
        this.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.085F);
        return data;
    }

    public static ItemStack createBannerShield() {
        ItemStack shield = new ItemStack(Items.SHIELD);
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("id", "minecraft:banner");
        blockEntityTag.putInt("Base", 0);
        ListTag patterns = new ListTag();
        patterns.add(bannerPattern("cs", 14));
        patterns.add(bannerPattern("hh", 14));
        patterns.add(bannerPattern("tr", 0));
        patterns.add(bannerPattern("tl", 0));
        patterns.add(bannerPattern("cs", 14));
        blockEntityTag.put("Patterns", patterns);
        shield.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
        return shield;
    }

    private static CompoundTag bannerPattern(String pattern, int color) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Pattern", pattern);
        tag.putInt("Color", color);
        return tag;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isRecruiter(target)) {
            return false;
        }
        if (target instanceof TemplarEntity || (target instanceof Factioned factioned && factioned.getFaction() == Faction.HOLY)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isHired();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
        tag.putInt("HireTicks", this.remainingHireTicks);
        tag.putInt("RecruiterGold", this.recruiterGoldGiven);
        if (this.recruiterUUID != null) {
            tag.putUUID("Recruiter", this.recruiterUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
        this.remainingHireTicks = tag.getInt("HireTicks");
        this.recruiterGoldGiven = tag.getInt("RecruiterGold");
        if (tag.hasUUID("Recruiter")) {
            this.recruiterUUID = tag.getUUID("Recruiter");
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

    private static class FollowRecruiterGoal extends Goal {
        private final TemplarEntity templar;
        private Player recruiter;
        private int timeToRecalcPath;

        FollowRecruiterGoal(TemplarEntity templar) {
            this.templar = templar;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.templar.isHired()) {
                return false;
            }
            Player owner = this.templar.getRecruiter();
            if (owner == null || owner.isSpectator() || this.templar.distanceToSqr(owner) < 16.0D) {
                return false;
            }
            this.recruiter = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.templar.isHired()
                    && this.recruiter != null
                    && this.recruiter.isAlive()
                    && !this.templar.getNavigation().isDone()
                    && this.templar.distanceToSqr(this.recruiter) > 9.0D;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.recruiter = null;
            this.templar.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.recruiter == null) {
                return;
            }
            this.templar.getLookControl().setLookAt(this.recruiter, 10.0F, this.templar.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (this.templar.distanceToSqr(this.recruiter) >= 1024.0D && this.templar.level() instanceof ServerLevel) {
                    this.templar.teleportTo(this.recruiter.getX(), this.recruiter.getY(), this.recruiter.getZ());
                } else {
                    this.templar.getNavigation().moveTo(this.recruiter, 1.1D);
                }
            }
        }
    }

    private static class ProtectRecruiterGoal extends TargetGoal {
        private final TemplarEntity templar;
        private LivingEntity candidate;

        ProtectRecruiterGoal(TemplarEntity templar) {
            super(templar, false);
            this.templar = templar;
        }

        @Override
        public boolean canUse() {
            if (!this.templar.isHired()) {
                return false;
            }
            Player recruiter = this.templar.getRecruiter();
            if (recruiter == null || !recruiter.isAlive()) {
                return false;
            }
            LivingEntity attacker = recruiter.getLastHurtByMob();
            LivingEntity victim = recruiter.getLastHurtMob();
            if (attacker != null && attacker.isAlive() && this.templar.canAttack(attacker) && !this.templar.isRecruiter(attacker)) {
                this.candidate = attacker;
                return true;
            }
            if (victim != null && victim.isAlive() && this.templar.canAttack(victim) && !this.templar.isRecruiter(victim)) {
                this.candidate = victim;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.templar.setTarget(this.candidate);
            super.start();
        }
    }
}
