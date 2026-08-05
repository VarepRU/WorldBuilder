package ru.varep.worldbuilder.img.data;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Set;

public record RegionMapData(
        int width,
        int height,
        int scale,
        int[] regionIds,
        Set<ResourceKey<Biome>> allBiomes,
        ResourceKey<Biome>[] biomes,
        Map<Integer, String> regionNames,
        Map<Integer, Integer> regionColors
) implements MapData {

    public int getRegionId(int blockX, int blockZ) {
        return regionIds[sample(blockX, blockZ)];
    }

    public ResourceKey<Biome> getBiome(int blockX, int blockZ) {
        return biomes[sample(blockX, blockZ)];
    }

    public String getRegionName(int regionId) {
        return regionNames.getOrDefault(regionId, "Unknown");
    }

    public int getRegionColor(int regionId) {
        return regionColors.getOrDefault(regionId, 0xFF00FF);
    }

    public Set<ResourceKey<Biome>> getAllPossibleBiomes() {
        return allBiomes;
    }

    private int sample(int blockX, int blockZ) {
        int px = Math.floorMod(blockX / scale, width);
        int pz = Math.floorMod(blockZ / scale, height);
        return pz * width + px;
    }
}
