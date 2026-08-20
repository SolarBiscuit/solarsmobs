package com.solarbiscuit.entity.femboy;

import com.solarbiscuit.advancement.ModAdvancements;
import com.solarbiscuit.compat.sophisticatedbackpacks.FemboyBackpackCompat;
import com.solarbiscuit.compat.sophisticatedstorage.FemboyStorageCompat;
import com.solarbiscuit.faction.Faction;
import com.solarbiscuit.faction.FactionRelations;
import com.solarbiscuit.faction.Factioned;
import com.solarbiscuit.inventory.femboy.FemboyMenu;
import com.solarbiscuit.registry.ModEntities;
import com.solarbiscuit.registry.ModFluids;
import com.solarbiscuit.util.NameLists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class FemboyEntity extends TamableAnimal implements Factioned {
    private SimpleContainer inventory = new SimpleContainer(27);
    private boolean isLargeChest = false;
    private boolean lastNearbySneak;
    private final long[] sneakPressTimes = new long[3];
    private int sneakPressCount;
    private int sneakMimicTicks;

    public FemboyEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public Faction getFaction() {
        return FactionRelations.femboyFaction(this);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public boolean isLargeChest() {
        return this.isLargeChest;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    public boolean isValidTarget(LivingEntity target) {
        return FactionRelations.canFemboyTarget(this, target);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new StayStillWhileAccessedGoal(this));
        this.goalSelector.addGoal(2, new FemboySleepWithOwnerGoal(this));

        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true) {
            @Override
            public boolean canUse() {
                return !FemboyEntity.this.getMainHandItem().isEmpty() && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return !FemboyEntity.this.getMainHandItem().isEmpty() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.5D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.5D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (FemboyEntity.this.getMainHandItem().isEmpty() || FemboyEntity.this.getOwner() == null || !super.canUse()) {
                    return false;
                }
                LivingEntity attacker = FemboyEntity.this.getOwner().getLastHurtByMob();
                return FactionRelations.canFemboyRetaliate(FemboyEntity.this, attacker)
                        || FactionRelations.canFemboyTarget(FemboyEntity.this, attacker);
            }
        });
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (FemboyEntity.this.getMainHandItem().isEmpty() || !super.canUse()) return false;
                return FactionRelations.canFemboyRetaliate(FemboyEntity.this, FemboyEntity.this.getLastHurtByMob());
            }
        });
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemstack.is(Items.EMERALD) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (this.isTame() && this.isOwnedBy(player)) {

            InteractionResult storageInteract = FemboyStorageCompat.tryInteract(this, player, hand, itemstack);
            if (storageInteract.consumesAction()) {
                return storageInteract;
            }

            InteractionResult backpackInteract = FemboyBackpackCompat.tryInteract(this, player, hand, itemstack);
            if (backpackInteract.consumesAction()) {
                return backpackInteract;
            }

            if (itemstack.is(Items.CHEST) && !this.isLargeChest && !FemboyStorageCompat.isLoaded()) {
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                this.upgradeToLargeChest();
                this.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (itemstack.isEdible() && this.getHealth() < this.getMaxHealth()) {
                this.heal((float) itemstack.getItem().getFoodProperties(itemstack, player).getNutrition());
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (itemstack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                var fluidHandler = itemstack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
                if (fluidHandler != null) {
                    int filled = fluidHandler.fill(new FluidStack(ModFluids.FEMBOY_MILK.get(), 1000), IFluidHandlerItem.FluidAction.SIMULATE);
                    if (filled == 1000) {
                        fluidHandler.fill(new FluidStack(ModFluids.FEMBOY_MILK.get(), 1000), IFluidHandlerItem.FluidAction.EXECUTE);
                        player.setItemInHand(hand, fluidHandler.getContainer());
                        this.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            if (itemstack.isEmpty() && this.isLookingAtBack(player)
                    && FemboyBackpackCompat.handleOpen(player, this)) {
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown() && itemstack.isEmpty()) {
                if (!this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()
                        && this.isLookingAtBack(player)) {
                    return InteractionResult.PASS;
                }
                this.openCustomInventory(player);
                return InteractionResult.SUCCESS;
            }

            if (itemstack.isEmpty() && !player.isShiftKeyDown()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                if (this.isOrderedToSit()) {
                    this.getNavigation().stop();
                    this.setTarget(null);
                }
                return InteractionResult.SUCCESS;
            }

        } else if (!this.isTame() && itemstack.is(Items.EMERALD)) {
            if (!player.getAbilities().instabuild) itemstack.shrink(1);
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                if (!this.hasCustomName()) {
                    this.setCustomName(Component.literal(NameLists.randomFemboyName(this.random)));
                    this.setCustomNameVisible(true);
                }
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART,
                            this.getX(), this.getY() + this.getBbHeight(), this.getZ(),
                            8, 0.4D, 0.3D, 0.4D, 0.05D);
                }
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private boolean isLookingAtBack(Player player) {
        Vec3 viewVector = player.getViewVector(1.0F).normalize();
        Vec3 entityLook = this.getViewVector(1.0F).normalize();
        return viewVector.dot(entityLook) > 0.0D;
    }

    private void upgradeToLargeChest() {
        this.isLargeChest = true;
        SimpleContainer newInv = new SimpleContainer(54);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            newInv.setItem(i, this.inventory.getItem(i));
        }
        this.inventory = newInv;
    }

    private void openCustomInventory(Player player) {
        if (FemboyStorageCompat.isLoaded()) {
            FemboyStorageCompat.open(player, this);
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal(isLargeChest ? "Large Femboy Inventory" : "Femboy Inventory");
                }
                @Override
                public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
                    return new FemboyMenu(id, playerInv, FemboyEntity.this, isLargeChest);
                }
            }, buf -> {
                buf.writeInt(this.getId());
                buf.writeBoolean(this.isLargeChest);
            });
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsLargeChest", this.isLargeChest);
        ListTag listtag = new ListTag();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte) i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }
        compound.put("Items", listtag);
        FemboyStorageCompat.save(this, compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.isLargeChest = compound.getBoolean("IsLargeChest");
        this.inventory = new SimpleContainer(this.isLargeChest ? 54 : 27);
        ListTag listtag = compound.getList("Items", 10);
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundtag));
            }
        }
        FemboyStorageCompat.load(this, compound);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.tickSneakMimic();
        if (!this.level().isClientSide) {
            FemboyStorageCompat.tick(this);
            FemboyBackpackCompat.tick(this);
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (FemboyStorageCompat.isLoaded()) {
            FemboyStorageCompat.dropContents(this);
        } else {
            for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    this.spawnAtLocation(itemstack);
                }
            }
        }
        com.solarbiscuit.compat.curios.CuriosCompat.dropFemboySlots(this);
    }

    @Override
    public void startSleeping(BlockPos pos) {
        // Sit beside the owner instead of occupying the bed.
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.FEMBOY.get().create(level);
    }

    private void tickSneakMimic() {
        if (this.level().isClientSide) {
            return;
        }
        if (this.sneakMimicTicks > 0) {
            int cycle = 7;
            int elapsed = 21 - this.sneakMimicTicks;
            this.setShiftKeyDown((elapsed % cycle) < 4);
            this.sneakMimicTicks--;
            if (this.sneakMimicTicks == 0) {
                this.setShiftKeyDown(false);
            }
            return;
        }
        if (this.isInSittingPose() || this.isOrderedToSit()) {
            this.lastNearbySneak = false;
            return;
        }
        Player inFront = this.findPlayerInFront();
        boolean sneaking = inFront != null && inFront.isShiftKeyDown();
        if (sneaking && !this.lastNearbySneak) {
            long now = this.level().getGameTime();
            if (this.sneakPressCount < 3) {
                this.sneakPressTimes[this.sneakPressCount++] = now;
            } else {
                this.sneakPressTimes[0] = this.sneakPressTimes[1];
                this.sneakPressTimes[1] = this.sneakPressTimes[2];
                this.sneakPressTimes[2] = now;
            }
            if (this.sneakPressCount >= 3 && now - this.sneakPressTimes[0] <= 10L) {
                this.sneakMimicTicks = 21;
                this.sneakPressCount = 0;
            }
        }
        this.lastNearbySneak = sneaking;
    }

    private Player findPlayerInFront() {
        AABB box = this.getBoundingBox().inflate(3.5D, 1.0D, 3.5D);
        Player best = null;
        for (Player player : this.level().getEntitiesOfClass(Player.class, box)) {
            if (player.isSpectator()) {
                continue;
            }
            Vec3 toPlayer = player.position().subtract(this.position());
            Vec3 horizontal = new Vec3(toPlayer.x, 0.0D, toPlayer.z);
            if (horizontal.lengthSqr() < 0.0001D) {
                continue;
            }
            Vec3 look = this.getLookAngle();
            Vec3 lookFlat = new Vec3(look.x, 0.0D, look.z);
            if (lookFlat.lengthSqr() < 0.0001D) {
                continue;
            }
            if (horizontal.normalize().dot(lookFlat.normalize()) <= 0.35D) {
                continue;
            }
            if (player.isShiftKeyDown()) {
                return player;
            }
            if (best == null) {
                best = player;
            }
        }
        return best;
    }

    public boolean isBeingAccessedByOwner() {
        if (!(this.getOwner() instanceof Player player)) {
            return false;
        }
        if (player.containerMenu instanceof FemboyMenu menu) {
            return menu.getFemboy() == this;
        }
        return FemboyStorageCompat.isOpenFor(player, this) || FemboyBackpackCompat.isOpenFor(player, this);
    }

    class StayStillWhileAccessedGoal extends Goal {
        private final FemboyEntity femboy;

        StayStillWhileAccessedGoal(FemboyEntity femboy) {
            this.femboy = femboy;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.femboy.isTame() && this.femboy.isBeingAccessedByOwner();
        }

        @Override
        public boolean canContinueToUse() {
            return this.femboy.isBeingAccessedByOwner();
        }

        @Override
        public void start() {
            this.femboy.getNavigation().stop();
            this.femboy.setTarget(null);
        }

        @Override
        public void tick() {
            this.femboy.getNavigation().stop();
        }

        @Override
        public void stop() {
        }
    }

    class FemboySleepWithOwnerGoal extends Goal {
        private final FemboyEntity femboy;
        private BlockPos sitBeside;

        public FemboySleepWithOwnerGoal(FemboyEntity entity) {
            this.femboy = entity;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.femboy.isTame() || this.femboy.isOrderedToSit() || this.femboy.getOwner() == null) {
                return false;
            }
            Player owner = (Player) this.femboy.getOwner();
            return owner.isSleeping() && this.femboy.distanceToSqr(owner) < 64.0D;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = (Player) this.femboy.getOwner();
            return owner != null && owner.isSleeping();
        }

        @Override
        public void start() {
            Player owner = (Player) this.femboy.getOwner();
            if (owner != null && owner.isSleeping()) {
                this.sitBeside = owner.blockPosition();
                this.femboy.getNavigation().moveTo(this.sitBeside.getX() + 0.5D, this.sitBeside.getY(), this.sitBeside.getZ() + 0.5D, 1.0D);
            }
        }

        @Override
        public void tick() {
            Player owner = (Player) this.femboy.getOwner();
            if (owner == null || !owner.isSleeping()) {
                return;
            }
            if (this.femboy.distanceToSqr(owner) <= 16.0D) {
                this.femboy.getNavigation().stop();
                this.femboy.setInSittingPose(true);
                if (owner instanceof ServerPlayer serverPlayer) {
                    ModAdvancements.award(serverPlayer, ModAdvancements.BEST_SLEEP, "slept_together");
                }
            } else if (this.femboy.getNavigation().isDone() && this.sitBeside != null) {
                this.femboy.getNavigation().moveTo(this.sitBeside.getX() + 0.5D, this.sitBeside.getY(), this.sitBeside.getZ() + 0.5D, 1.0D);
            }
        }

        @Override
        public void stop() {
            if (!this.femboy.isOrderedToSit()) {
                this.femboy.setInSittingPose(false);
            }
            this.sitBeside = null;
        }
    }
}