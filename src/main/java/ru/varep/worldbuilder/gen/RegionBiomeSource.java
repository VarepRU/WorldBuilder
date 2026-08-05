package ru.varep.worldbuilder.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;


import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.HashMap;
import java.util.Map;


import net.minecraft.world.level.biome.Climate;

import ru.varep.worldbuilder.img.data.RegionMapData;
import ru.varep.worldbuilder.img.data.WorldbuilderMaps;


import java.util.stream.Stream;

public class RegionBiomeSource extends BiomeSource {

    public static final MapCodec<RegionBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ResourceLocation.CODEC
                            .fieldOf("map")
                            .forGetter(s -> s.mapId),
                    RegistryCodecs.homogeneousList(Registries.BIOME)
                            .fieldOf("possible_biomes")
                            .forGetter(s -> s.possibleBiomes)
            ).apply(inst, RegionBiomeSource::new));

    private final ResourceLocation mapId;
    private final HolderSet<Biome> possibleBiomes;
    private final Map<ResourceLocation, Holder<Biome>> biomeCache;
    private final Holder<Biome> fallbackBiome;

    public RegionBiomeSource(ResourceLocation mapId,
                                HolderSet<Biome> possibleBiomes) {
        super();
        this.mapId = mapId;
        this.possibleBiomes = possibleBiomes;
        this.biomeCache = new HashMap<>();

        possibleBiomes.stream().forEach(holder -> {
            holder.unwrapKey().ifPresent(key ->
                    biomeCache.put(key.location(), holder)
            );
        });

        this.fallbackBiome = possibleBiomes.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "[WORLDBUILDER] RegionMapBiomeSource requires at least one biome"
                ));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return possibleBiomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ,
                                       Climate.Sampler sampler) {

        RegionMapData map = WorldbuilderMaps.getRegion(mapId);
        if (map == null) {
            return fallbackBiome;
        }

        int blockX = quartX << 2;
        int blockZ = quartZ << 2;

        ResourceKey<Biome> biomeKey = map.getBiome(blockX, blockZ);
        if (biomeKey == null) {
            return fallbackBiome;
        }

        return biomeCache.getOrDefault(biomeKey.location(), fallbackBiome);
    }
}


