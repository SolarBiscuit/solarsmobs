package com.solarbiscuit.compat.jade;

import com.solarbiscuit.entity.templar.TemplarEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TemplarHireProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final String HIRE_TICKS_TAG = "HireTicks";

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof TemplarEntity templar) || !templar.isHired()) {
            return;
        }
        data.putInt(HIRE_TICKS_TAG, templar.getRemainingHireTicks());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        int ticks = accessor.getServerData().getInt(HIRE_TICKS_TAG);
        if (ticks <= 0) {
            return;
        }
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String clock = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        tooltip.add(Component.translatable("jade.solarsmobs.templar.following", clock));
    }

    @Override
    public ResourceLocation getUid() {
        return SolarsMobsJadePlugin.TEMPLAR_HIRE;
    }
}
