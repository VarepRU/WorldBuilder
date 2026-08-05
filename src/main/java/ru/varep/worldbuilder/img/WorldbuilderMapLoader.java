package ru.varep.worldbuilder.img;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.img.config.MapConfig;
import ru.varep.worldbuilder.img.config.RegionConfig;

import ru.varep.worldbuilder.img.noise.core.NoiseSampler;
import ru.varep.worldbuilder.img.data.*;
import ru.varep.worldbuilder.img.noise.core.NoiseSamplerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class WorldbuilderMapLoader {
    private WorldbuilderMapLoader() {}

    private static final String MAPS_DIR = "worldbuilder/maps";

    //
    //выполняется при старте сервера
    public static void reload(MinecraftServer server) {
        reload(server.getResourceManager(), server.registryAccess(), server.overworld().getSeed());
    }


    public static void reload(ResourceManager rm, HolderLookup.Provider lookupProvider, long worldSeed) {
        MapIndex index = loadIndex(rm);
        Map<ResourceLocation, MapData> out = new HashMap<>();

        for (ResourceLocation id : index.maps()) {
            tryLoad(rm, lookupProvider, worldSeed, id).ifPresent(data -> out.put(id, data));
        }

        WorldbuilderMaps.replaceAll(out);
    }

    //ОБРАБОТЧИК КАРТЫ

    private static Optional<MapData> tryLoad(
            ResourceManager rm,
            HolderLookup.Provider lookupProvider,
            long worldSeed,
            ResourceLocation id
    ) {
        ResourceLocation pngId = mapPngId(id);
        ResourceLocation jsonId = mapJsonId(id);

        MapConfig cfg = loadConfig(rm, lookupProvider, jsonId);
        WorldbuilderMod.LOGGER.info("[WORLDBUILDER] Found config {} with mode={}", jsonId, cfg.mode());

        try {
            BufferedImage img = loadPng(rm, pngId);
            if (img == null) return Optional.empty();

            int w = img.getWidth();
            int h = img.getHeight();
            int scale = Math.max(1, cfg.scale());

            WorldbuilderMod.LOGGER.info("[WORLDBUILDER] Loading map {}", id);

            return switch (cfg.mode()) {
                case BIOME -> Optional.of(loadBiomeMode(cfg, img, w, h, scale));
                case REGIONS -> Optional.of(loadRegionMode(cfg, img, w, h, scale, worldSeed));
                case GRADIENT, STEPS -> Optional.of(loadHeightMode(cfg, img, w, h, scale));
            };
        } catch (Exception e) {
            WorldbuilderMod.LOGGER.error("[WORLDBUILDER] Failed to load map {}", id, e);
            return Optional.empty();
        }
    }



    private static @Nullable BufferedImage loadPng(ResourceManager rm, ResourceLocation pngId) throws IOException {
        Resource pngRes = rm.getResource(pngId).orElse(null);
        if (pngRes == null) return null;

        try (var in = pngRes.open()) {
            return ImageIO.read(in);
        }
    }

    //КАРТА БИОМОВ

    private static MapData loadBiomeMode(MapConfig cfg, BufferedImage img, int w, int h, int scale) {
        int[] rgb = loadRgb(img);

        ResourceLocation fallback = cfg.biomeMap().isEmpty()
                ? ResourceLocation.withDefaultNamespace("plains")
                : cfg.biomeMap().values().iterator().next();

        return new BiomeMapData(w, h, scale, rgb, cfg.biomeMap(), fallback);
    }

    //МАССИВ ПИКСЕЛЕЙ


    private static int[] loadRgb (BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] argb = new int[w * h];
        img.getRGB(0, 0, w, h, argb, 0, w);
        for (int i = 0; i < argb.length; i++) argb[i] &= 0xFFFFFF;
        return argb;
    }

    //КАРТА РЕГИОНОВ

    private static MapData loadRegionMode(
            MapConfig cfg,
            BufferedImage img,
            int w,
            int h,
            int scale,
            long worldSeed
    ) {

        List<RegionConfig> regions = cfg.regions();
        WorldbuilderMod.LOGGER.info("[WORLDBUILDER] Regions count={}", regions.size());

        Set<ResourceKey<Biome>> allBiomes = collectAllRegionBiomes(regions);

        Map<Integer, Integer> colorToRegionId = new HashMap<>();
        Map<Integer, String> regionNames = new HashMap<>();
        Map<Integer, Integer> regionColors = new HashMap<>();

        buildRegionIndex(regions, colorToRegionId, regionNames, regionColors);

        Map<Integer, NoiseSampler> regionSamplers = buildRegionSamplers(regions, worldSeed);

        int[] regionIds = new int[w * h];
        @SuppressWarnings("unchecked")
        ResourceKey<Biome>[] biomes = new ResourceKey[w * h];

        int[] argb = loadRgb(img);



        for (int z = 0; z < h; z++) {
            for (int x = 0; x < w; x++) {
                int idx = z * w + x;
                int rgb = argb[idx] & 0xFFFFFF;
                Integer regionId = colorToRegionId.get(rgb);

                if (regionId == null) {
                    regionIds[idx] = -1;
                    biomes[idx] = null;
                    continue;
                }

                regionIds[idx] = regionId;
                RegionConfig region = regions.get(regionId);

                int blockX = x * scale;
                int blockZ = z * scale;

                NoiseSampler sampler = regionSamplers.get(regionId);
                double noiseValue = sampler.sample(blockX, blockZ);

                biomes[idx] = region.biomes().returnBiome(noiseValue);
            }
        }

        return new RegionMapData(w, h, scale, regionIds, allBiomes, biomes, regionNames, regionColors);
    }

    //СПИСОК ВСЕХ БИОМОВ

    private static Set<ResourceKey<Biome>> collectAllRegionBiomes(List<RegionConfig> regions) {
        Set<ResourceKey<Biome>> allBiomes = new HashSet<>();
        for (RegionConfig region : regions) {
            allBiomes.add(region.biomes().common());
            allBiomes.addAll(region.biomes().variants().keySet());
        }
        return allBiomes;
    }

    //СЛОВАЬ РЕГИОНОВ

    private static void buildRegionIndex(
            List<RegionConfig> regions,
            Map<Integer, Integer> colorToRegionId,
            Map<Integer, String> regionNames,
            Map<Integer, Integer> regionColors
    ) {
        for (int i = 0; i < regions.size(); i++) {
            RegionConfig region = regions.get(i);

            colorToRegionId.put(region.color(), i);
            regionNames.put(i, region.name());
            regionColors.put(i, region.color());
        }
    }

    //СЭМПЛЕРЫ ШУМА

    private static Map<Integer, NoiseSampler> buildRegionSamplers(List<RegionConfig> regions, long worldSeed) {
        Map<Integer, NoiseSampler> regionSamplers = new HashMap<>();
        for (int i = 0; i < regions.size(); i++) {
            RegionConfig region = regions.get(i);
            NoiseSampler sampler = NoiseSamplerFactory.create(region.noise().value(), worldSeed);
            regionSamplers.put(i, sampler);
        }
        return regionSamplers;
    }

    //КАРТЫ ВЫСОТ
    private static MapData loadHeightMode(MapConfig cfg, BufferedImage img, int w, int h, int scale) {
        float min = cfg.min();
        float max = cfg.max();
        float gradientScale = (max - min) / 255.0F;
        NavigableMap<Integer, Float> steps = cfg.steps();

        float[] values = new float[w * h];

        int[] argb = loadRgb(img);

        for (int z = 0; z < h; z++) {
            int row = z * w;
            for (int x = 0; x < w; x++) {
                int idx = row + x;

                int gray = toGray(argb[idx]);

                float v = (cfg.mode() == MapConfig.Mode.STEPS)
                        ? sampleSteps(gray, steps, min)
                        : (min + gray * gradientScale);

                values[idx] = v;
            }
        }

        return new HeightMapData(w, h, scale, min, max, values);
    }

    private static int toGray(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (r + g + b) / 3;
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

    private static MapConfig loadConfig(ResourceManager rm, HolderLookup.Provider lookupProvider, ResourceLocation jsonId) {
        try {
            Resource res = rm.getResource(jsonId)
                    .orElseThrow(() -> new IllegalStateException("[WORLDBUILDER] Missing config: " + jsonId));

            try (var reader = res.openAsReader()) {
                com.google.gson.JsonElement json = com.google.gson.JsonParser.parseReader(reader);

                var ops = lookupProvider.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);

                return MapConfig.CODEC.parse(ops, json)
                        .getOrThrow(error -> new IllegalStateException("[WORLDBUILDER] Failed to parse " + jsonId + ": " + error));
            }
        } catch (Exception e) {
            throw new RuntimeException("[WORLDBUILDER] Failed to load config " + jsonId, e);
        }
    }

    private static ResourceLocation mapPngId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(), MAPS_DIR + "/" + id.getPath() + ".png"
        );
    }

    private static ResourceLocation mapJsonId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(), MAPS_DIR + "/" + id.getPath() + ".json"
        );
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