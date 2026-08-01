package ru.varep.worldbuilder.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import ru.varep.worldbuilder.img.WorldbuilderMaps;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WorldbuilderBiomeSource extends BiomeSource {

    public static final MapCodec<WorldbuilderBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ResourceLocation.CODEC
                            .fieldOf("map")
                            .forGetter(s -> s.mapId),
                    RegistryCodecs.homogeneousList(Registries.BIOME)
                            .fieldOf("possible_biomes")
                            .forGetter(s -> s.possibleBiomes)
            ).apply(inst, WorldbuilderBiomeSource::new));

    private final ResourceLocation mapId;
    private final HolderSet<Biome> possibleBiomes;
    private final Map<ResourceLocation, Holder<Biome>> biomeCache;
    private final Holder<Biome> fallbackBiome;

    public WorldbuilderBiomeSource(ResourceLocation mapId,
                                   HolderSet<Biome> possibleBiomes) {
        super();
        this.mapId = mapId;
        this.possibleBiomes = possibleBiomes;
        this.biomeCache = new HashMap<>();

        // кэш биомов
        possibleBiomes.stream().forEach(holder -> {
            holder.unwrapKey().ifPresent(key ->
                    biomeCache.put(key.location(), holder)
            );
        });

        // фолбэк
        this.fallbackBiome = possibleBiomes.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "WorldbuilderBiomeSource requires at least one biome"
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

        WorldbuilderMaps.BiomeMapData map = WorldbuilderMaps.getBiome(mapId);
        if (map == null) {
            return fallbackBiome;
        }

        int blockX = quartX << 2;
        int blockZ = quartZ << 2;

        ResourceLocation biomeId = map.sampleBiome(blockX, blockZ);

        return biomeCache.getOrDefault(biomeId, fallbackBiome);
    }
}


