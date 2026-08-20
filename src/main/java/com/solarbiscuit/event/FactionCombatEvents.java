package com.solarbiscuit.event;

import com.solarbiscuit.SolarsMobs;
import com.solarbiscuit.entity.archer.ArcherEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolarsMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionCombatEvents {
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ArcherEntity archer) || archer.level().isClientSide()) {
            return;
        }
        if ((archer.tickCount + archer.getId()) % 20 != 0) {
            return;
        }
        double range = archer.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB box = archer.getBoundingBox().inflate(range);
        for (Monster monster : archer.level().getEntitiesOfClass(Monster.class, box,
                candidate -> !(candidate instanceof ArcherEntity))) {
            if (monster.getTarget() != null) {
                continue;
            }
            if (monster.getSensing().hasLineOfSight(archer) && monster.canAttack(archer)) {
                monster.setTarget(archer);
            }
        }
    }
}
