package com.solarbiscuit.faction;

import com.solarbiscuit.compat.curios.CuriosCompat;
import com.solarbiscuit.entity.femboy.FemboyEntity;
import com.solarbiscuit.entity.thief.ThiefEntity;
import com.solarbiscuit.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.UUID;

/**
 * Shared targeting rules for factioned mobs. Keep mob AI thin; put reusable policy here.
 */
public final class FactionRelations {
    private FactionRelations() {}

    public static boolean hasThievesGuildProtection(LivingEntity entity) {
        Item necklace = ModItems.THIEVES_GUILD_NECKLACE.get();
        return isHolding(entity, necklace) || CuriosCompat.isEquipped(entity, necklace);
    }

    public static boolean hasHolyCross(LivingEntity entity) {
        return isHolding(entity, ModItems.HOLY_CROSS.get())
                || CuriosCompat.isEquipped(entity, ModItems.HOLY_CROSS.get());
    }

    private static boolean isHolding(LivingEntity entity, Item item) {
        return entity.getMainHandItem().is(item) || entity.getOffhandItem().is(item);
    }

    /** Evil thieves ignore necklace wearers until provoked. */
    public static boolean thiefShouldHuntPlayer(Player player) {
        return !hasThievesGuildProtection(player);
    }

    public static boolean isHolyFoe(LivingEntity target) {
        if (!(target instanceof Factioned factioned)) {
            return false;
        }
        Faction faction = factioned.getFaction();
        return faction == Faction.EVIL || faction == Faction.DEMONIC;
    }

    public static boolean sameOwner(LivingEntity a, LivingEntity b) {
        UUID ownerA = ownerId(a);
        UUID ownerB = ownerId(b);
        return ownerA != null && ownerA.equals(ownerB);
    }

    private static UUID ownerId(LivingEntity entity) {
        if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        return null;
    }

    /**
     * Untamed femboy = Virtue. Tamed = Neutral to everyone except the owner (never fights owner).
     */
    public static Faction femboyFaction(FemboyEntity femboy) {
        return femboy.isTame() ? Faction.NEUTRAL : Faction.VIRTUE;
    }

    public static boolean canFemboyRetaliate(FemboyEntity femboy, LivingEntity attacker) {
        if (attacker == null || !attacker.isAlive()) {
            return false;
        }
        if (femboy.isOwnedBy(attacker)) {
            return false;
        }
        if (femboyFaction(femboy) == Faction.VIRTUE && attacker instanceof Player) {
            return false;
        }
        if (attacker instanceof TamableAnimal other && other.isTame() && sameOwner(femboy, other)) {
            return false;
        }
        return true;
    }

    /**
     * Proactive / assist targeting for femboys (not retaliation).
     * Respects thieves-guild necklace: don't pick fights with thieves while the owner is protected.
     */
    public static boolean canFemboyTarget(FemboyEntity femboy, LivingEntity target) {
        if (target == null || !target.isAlive() || target instanceof Player) {
            return false;
        }
        if (target instanceof TamableAnimal other && other.isTame() && sameOwner(femboy, other)) {
            return false;
        }
        if (target instanceof ThiefEntity
                && femboy.isTame()
                && femboy.getOwner() instanceof Player owner
                && hasThievesGuildProtection(owner)
                && owner.getLastHurtByMob() != target
                && femboy.getLastHurtByMob() != target) {
            return false;
        }
        return true;
    }
}
