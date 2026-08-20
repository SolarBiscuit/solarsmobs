package com.solarbiscuit.client;

import com.solarbiscuit.client.archer.ArcherRenderer;
import com.solarbiscuit.client.arborist.ArboristRenderer;
import com.solarbiscuit.client.endwarrior.EndWarriorRenderer;
import com.solarbiscuit.client.femboy.FemboyRenderer;
import com.solarbiscuit.client.templar.TemplarRenderer;
import com.solarbiscuit.client.thief.ThiefRenderer;
import com.solarbiscuit.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ClientMobCatalog {
    private ClientMobCatalog() {}

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (RendererEntry<?> entry : ALL) {
            entry.register(event);
        }
    }

    private static final List<RendererEntry<?>> ALL = List.of(
            new RendererEntry<>(ModEntities.FEMBOY, FemboyRenderer::new),
            new RendererEntry<>(ModEntities.THIEF, ThiefRenderer::new),
            new RendererEntry<>(ModEntities.TEMPLAR, TemplarRenderer::new),
            new RendererEntry<>(ModEntities.END_WARRIOR, EndWarriorRenderer::new),
            new RendererEntry<>(ModEntities.ARCHER, ArcherRenderer::new),
            new RendererEntry<>(ModEntities.ARBORIST, ArboristRenderer::new)
    );

    private record RendererEntry<T extends Entity>(
            RegistryObject<EntityType<T>> type,
            EntityRendererProvider<T> provider
    ) {
        private void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(type.get(), provider);
        }
    }
}
