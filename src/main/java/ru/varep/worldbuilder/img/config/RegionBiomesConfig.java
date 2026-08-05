package ru.varep.worldbuilder.img.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public record RegionBiomesConfig(ResourceKey<Biome> common,
                                 Map<ResourceKey<Biome>, VariantRange> variants) {

    private static final Codec<ResourceKey<Biome>> BIOME_KEY_CODEC =
            ResourceKey.codec(Registries.BIOME);

    private static final Codec<Map<ResourceKey<Biome>, VariantRange>> VARIANTS_CODEC =
            Codec.unboundedMap(Codec.STRING, VariantRange.CODEC).xmap(
                    map -> {
                        var out = new HashMap<ResourceKey<Biome>, VariantRange>();
                        for (var e : map.entrySet()) {
                            out.put(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(e.getKey())), e.getValue());
                        }
                        return out;
                    },
                    map -> {
                        var out = new HashMap<String, VariantRange>();
                        for (var e : map.entrySet()) {
                            out.put(e.getKey().location().toString(), e.getValue());
                        }
                        return out;
                    }
            );

    public static final Codec<RegionBiomesConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BIOME_KEY_CODEC.fieldOf("common").forGetter(RegionBiomesConfig::common),
            VARIANTS_CODEC.optionalFieldOf("variants", new HashMap<>()).forGetter(RegionBiomesConfig::variants)
    ).apply(inst, RegionBiomesConfig::new));

    public ResourceKey<Biome> returnBiome(double value) {
        for (var entry : variants.entrySet()) {
            VariantRange range = entry.getValue();
            if (value >= range.min() && value < range.max()) {
                return entry.getKey();
            }
        }
        return common;
    }
}

