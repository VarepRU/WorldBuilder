package ru.varep.worldbuilder.img;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import ru.varep.worldbuilder.WorldbuilderMod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

public final class WorldbuilderMapLoader {
    private WorldbuilderMapLoader() {}

    private static final String MAPS_DIR = "worldbuilder/maps";

    //
    //выполянется при загрузке мира или перезагрузке списка датапаков

    public static void reload(ResourceManager rm) {
        MapIndex index = loadIndex(rm);
        Map<ResourceLocation, WorldbuilderMaps.MapData> out = new HashMap<>();

        for (ResourceLocation id : index.maps()) {
            ResourceLocation pngId  = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(),
                    MAPS_DIR + "/" + id.getPath() + ".png"
            );

            ResourceLocation jsonId = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(),
                    MAPS_DIR + "/" + id.getPath() + ".json"
            );

            MapConfig cfg = loadConfig(rm, jsonId);

            try {
                Resource pngRes = rm.getResource(pngId).orElse(null);
                if (pngRes == null) continue;

                BufferedImage img;
                try (var in = pngRes.open()) {
                    img = ImageIO.read(in);
                }
                if (img == null) continue;

                int w = img.getWidth();
                int h = img.getHeight();
                float[] values = new float[w * h];

                float min = cfg.min();
                float max = cfg.max();
                float gradientScale = (max - min) / 255.0F;

                NavigableMap<Integer, Float> steps = cfg.steps();

                for (int z = 0; z < h; z++) {
                    for (int x = 0; x < w; x++) {
                        int argb = img.getRGB(x, z);

                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = (argb) & 0xFF;

                        int gray = (r + g + b) / 3;

                        float v;
                        if (cfg.mode() == MapConfig.Mode.STEPS) {
                            v = sampleSteps(gray, steps, min);
                        } else {
                            v = min + gray * gradientScale;
                        }

                        values[z * w + x] = v;
                    }
                }

                out.put(id, new WorldbuilderMaps.MapData(
                        w, h,
                        Math.max(1, cfg.scale()),
                        min, max,
                        values
                ));
            } catch (Exception e) {
                WorldbuilderMod.LOGGER.error("Failed to load map {}", id, e);
            }
        }

        WorldbuilderMaps.replaceAll(out);
    }

    //
    //загрузка списка всех карт

    private static MapIndex loadIndex(ResourceManager rm) {
        ResourceLocation indexId = ResourceLocation.fromNamespaceAndPath(
                "worldbuilder",
                MAPS_DIR + "/index.json"
        );

        try {
            Resource res = rm.getResource(indexId).orElse(null);
            if (res == null) return new MapIndex(java.util.List.of());

            try (var reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                var json = GsonHelper.parse(reader);
                return MapIndex.CODEC.parse(JsonOps.INSTANCE, json).result()
                        .orElseGet(() -> new MapIndex(java.util.List.of()));
            }
        } catch (Exception e) {
            return new MapIndex(java.util.List.of());
        }
    }

    //
    //подключение конфига

    private static MapConfig loadConfig(ResourceManager rm, ResourceLocation jsonId) {
        try {
            Resource res = rm.getResource(jsonId).orElse(null);
            if (res == null) return MapConfig.DEFAULT;

            try (var reader = new InputStreamReader(res.open(), StandardCharsets.UTF_8)) {
                var json = GsonHelper.parse(reader);
                return MapConfig.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(MapConfig.DEFAULT);
            }
        } catch (Exception e) {
            return MapConfig.DEFAULT;
        }
    }

    //
    //метод для заполнения массива ступеньками из конфига

    private static float sampleSteps(int gray, NavigableMap<Integer, Float> steps, float fallback) {
        if (steps == null || steps.isEmpty()) return fallback;

        var lo = steps.floorEntry(gray);
        var hi = steps.ceilingEntry(gray);

        if (lo == null) return hi.getValue();
        if (hi == null) return lo.getValue();

        int dlo = gray - lo.getKey();
        int dhi = hi.getKey() - gray;

        return (dlo <= dhi) ? lo.getValue() : hi.getValue();
    }
}