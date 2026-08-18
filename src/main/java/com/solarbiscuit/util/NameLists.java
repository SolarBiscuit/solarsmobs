package com.solarbiscuit.util;

import com.solarbiscuit.SolarsMobs;
import net.minecraft.util.RandomSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NameLists {
    private static final List<String> FEMBOY_NAMES = load("assets/" + SolarsMobs.MOD_ID + "/names/femboy.txt");

    private NameLists() {}

    public static String randomFemboyName(RandomSource random) {
        if (FEMBOY_NAMES.isEmpty()) {
            return "Femboy";
        }
        return FEMBOY_NAMES.get(random.nextInt(FEMBOY_NAMES.size()));
    }

    private static List<String> load(String path) {
        List<String> names = new ArrayList<>();
        InputStream stream = NameLists.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            return Collections.emptyList();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    names.add(trimmed);
                }
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
        return names;
    }
}
