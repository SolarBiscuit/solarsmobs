package com.solarbiscuit.client.skin;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loads humanoid PNG skins from {@code textures/entity/<folder>/} under any namespace.
 * Reuse for every player-model mob so each mob only supplies a folder name + fallback.
 */
public final class HumanoidSkinTextures {
    @SuppressWarnings("removal")
    public static final ResourceLocation STEVE = new ResourceLocation("minecraft", "textures/entity/steve.png");

    private final String folder;
    private final ResourceLocation fallback;
    private final List<ResourceLocation> textures = new ArrayList<>();
    private boolean loaded;

    public HumanoidSkinTextures(String folder) {
        this(folder, STEVE);
    }

    public HumanoidSkinTextures(String folder, ResourceLocation fallback) {
        this.folder = folder;
        this.fallback = fallback;
    }

    public ResourceLocation pick(int index) {
        ensureLoaded();
        if (textures.isEmpty()) {
            return fallback;
        }
        int safe = Math.floorMod(index, textures.size());
        return textures.get(safe);
    }

    public ResourceLocation pickFromUuid(java.util.UUID uuid) {
        ensureLoaded();
        if (textures.isEmpty()) {
            return fallback;
        }
        return textures.get(Math.floorMod(uuid.hashCode(), textures.size()));
    }

    public int skinCount() {
        ensureLoaded();
        return textures.size();
    }

    public List<ResourceLocation> all() {
        ensureLoaded();
        return Collections.unmodifiableList(textures);
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        Map<ResourceLocation, ?> resources = Minecraft.getInstance().getResourceManager()
                .listResources("textures/entity/" + folder, loc -> loc.getPath().endsWith(".png")
                        && !loc.getPath().endsWith("_aether.png"));
        textures.addAll(resources.keySet());
        textures.sort(ResourceLocation::compareTo);
        loaded = true;
    }
}
