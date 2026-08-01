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
        int rgb = sampleRgb(blockX, blockZ);
        ResourceLocation biome = biomeMap.get(rgb);
        return biome != null ? biome : fallbackBiome;
    }

    public int sampleRgb(int blockX, int blockZ) {
        int px = Math.floorDiv(blockX, scale);
        int pz = Math.floorDiv(blockZ, scale);
        px = Math.floorMod(px, width);
        pz = Math.floorMod(pz, height);
        return rgb[pz * width + px];
    }
}
