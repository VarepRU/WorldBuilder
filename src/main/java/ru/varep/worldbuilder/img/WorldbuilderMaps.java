package ru.varep.worldbuilder.img;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class WorldbuilderMaps {

    public sealed interface MapData permits HeightMapData, BiomeMapData {
        int width();

        int height();

        int scale();
    }

    public record HeightMapData(
            int width,
            int height,
            int scale,
            float min,
            float max,
            float[] values
    ) implements MapData {
        public float sample(int blockX, int blockZ) {

            float fx = (float) blockX / (float) scale;
            float fz = (float) blockZ / (float) scale;

            int x0 = fastFloor(fx);
            int z0 = fastFloor(fz);
            int x1 = x0 + 1;
            int z1 = z0 + 1;

            float tx = fx - x0;
            float tz = fz - z0;

            float v00 = getWrapped(x0, z0);
            float v10 = getWrapped(x1, z0);
            float v01 = getWrapped(x0, z1);
            float v11 = getWrapped(x1, z1);

            float a = lerp(v00, v10, tx);
            float b = lerp(v01, v11, tx);
            return lerp(a, b, tz);
        }

        private float getWrapped(int px, int pz) {
            int x = Math.floorMod(px, width);
            int z = Math.floorMod(pz, height);
            return values[z * width + x];
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        private static int fastFloor(float v) {
            int i = (int) v;
            return v < i ? i - 1 : i;
        }
    }

    public record BiomeMapData(
            int width,
            int height,
            int scale,
            int[] rgb,
            Map<Integer, ResourceLocation> biomeMap,
            ResourceLocation fallbackBiome
            ) implements MapData {
        public ResourceLocation sampleBiome(int blockX, int blockZ) {
            int rgb = sampleRgb(blockX, blockZ);

            ResourceLocation biome = biomeMap.get(rgb);
            if (biome != null) return biome;

            return fallbackBiome;
        }

        public int sampleRgb(int blockX, int blockZ) {
            int px = Math.floorDiv(blockX, scale);
            int pz = Math.floorDiv(blockZ, scale);
            px = Math.floorMod(px, width);
            pz = Math.floorMod(pz, height);
            return rgb[pz * width + px];
        }

    }

    private static volatile Map<ResourceLocation, MapData> MAPS = Map.of();

    public static @Nullable HeightMapData getHeight(ResourceLocation id) {
        MapData md = MAPS.get(id);
        if (md == null)
            return null;
        return (md instanceof HeightMapData hm) ? hm : null;
    }

    public static @Nullable BiomeMapData getBiome(ResourceLocation id) {
        MapData md = MAPS.get(id);
        if (md == null)
            return null;
        return (md instanceof BiomeMapData bm) ? bm : null;
    }


    public static void replaceAll(Map<ResourceLocation, MapData> maps) {
        MAPS = Map.copyOf(maps);
    }

    private WorldbuilderMaps() {}
}