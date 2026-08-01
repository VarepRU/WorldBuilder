package ru.varep.worldbuilder.img.MapData;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record BiomeMapData(
        int width,
        int height,
        int scale,
        int[] rgb,
        Map<Integer, ResourceLocation> biomeMap,
        ResourceLocation fallbackBiome
) implements MapData {

    public ResourceLocation sampleBiome(int blockX, int blockZ) {
        int warpedX = blockX + warp(blockX, blockZ, 0x9E3779B9);
        int warpedZ = blockZ + warp(blockX, blockZ, 0x7F4A7C15);
        int rgb = sampleRgb(warpedX, warpedZ);
        ResourceLocation biome = biomeMap.get(rgb);
        return biome != null ? biome : fallbackBiome;
    }

    private int warp(int x, int z, int seed) {
        int cell = scale * 2;
        int gx = Math.floorDiv(x, cell);
        int gz = Math.floorDiv(z, cell);

        int h = hash(gx, gz, seed);

        return (h & 0x7fffffff) % scale - scale / 2;
    }

    private static int hash(int x, int z, int seed) {
        int h = seed;
        h ^= x * 0x632BE59B;
        h ^= z * 0x85157AF5;
        h ^= (h >>> 16);
        h *= 0x7FEB352D;
        h ^= (h >>> 15);
        h *= 0x846CA68B;
        h ^= (h >>> 16);
        return h;
    }

    public int sampleRgb(int blockX, int blockZ) {
        int px = Math.floorDiv(blockX, scale);
        int pz = Math.floorDiv(blockZ, scale);
        px = Math.floorMod(px, width);
        pz = Math.floorMod(pz, height);
        return rgb[pz * width + px];
    }
}
