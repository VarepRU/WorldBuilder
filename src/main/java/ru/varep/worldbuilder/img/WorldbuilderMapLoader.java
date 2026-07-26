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

public final class WorldbuilderMapLoader {
    private WorldbuilderMapLoader() {}

    private static final String MAPS_DIR = "worldbuilder/maps";

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
                float scale = (max - min) / 255.0F;

                for (int z = 0; z < h; z++) {
                    for (int x = 0; x < w; x++) {
                        int argb = img.getRGB(x, z);
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = (argb) & 0xFF;
                        int gray = (r + g + b) / 3;
                        values[z * w + x] = min + gray * scale;
                    }
                }

                out.put(id, new WorldbuilderMaps.MapData(
                        w, h,
                        Math.max(1, cfg.scale()),
                        min, max,
                        values
                ));
            } catch (Exception e) {
                WorldbuilderMod.LOGGER.error("Failed to load map {}: {}", id, e.toString());
            }
        }

        WorldbuilderMaps.replaceAll(out);
    }

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
}