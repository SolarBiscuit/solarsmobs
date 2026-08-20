package com.solarbiscuit.event;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.archer.ArcherEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionCombatEvents {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Monster monster) || monster instanceof ArcherEntity) {
            return;
        }
        monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(monster, ArcherEntity.class, true));
    }
}
