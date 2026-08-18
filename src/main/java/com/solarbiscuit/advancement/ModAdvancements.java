package com.solarbiscuit.advancement;

import com.solarbiscuit.SolarsMobs;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {
    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation WHOLE_NEW_WORLD = id("whole_new_world");
    public static final ResourceLocation TAMED_FEMBOY = id("femboy/tamed");
    public static final ResourceLocation MILKED_FEMBOY = id("femboy/milked");
    public static final ResourceLocation BEST_SLEEP = id("femboy/best_sleep");
    public static final ResourceLocation NOT_TODAY = id("thief/not_today");
    public static final ResourceLocation BROTHERHOOD = id("thief/brotherhood");
    public static final ResourceLocation HOLY_WAR = id("templar/holy_war");
    public static final ResourceLocation DEFENDER_OF_THE_FAITH = id("templar/defender_of_the_faith");
    public static final ResourceLocation TITHE = id("templar/tithe");

    private ModAdvancements() {}

    public static void award(ServerPlayer player, ResourceLocation advancementId, String criterion) {
        if (player == null || player.server == null) {
            return;
        }
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    @SuppressWarnings("removal")
    private static ResourceLocation id(String path) {
        return new ResourceLocation(SolarsMobs.MOD_ID, path);
    }
}
