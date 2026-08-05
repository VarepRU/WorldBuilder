package ru.varep.worldbuilder.img.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;

import net.minecraft.resources.RegistryFixedCodec;
import ru.varep.worldbuilder.img.noise.core.NoiseDefinition;
import ru.varep.worldbuilder.reg.WorldbuilderRegistries;

public record RegionConfig(String name,
                           int color,
                           Holder<NoiseDefinition> noise,
                           RegionBiomesConfig biomes) {

    private static final Codec<Integer> COLOR_CODEC = Codec.STRING.xmap(
            s -> {
                String hex = s.startsWith("#") ? s.substring(1) : s;
                return Integer.parseInt(hex, 16);
            },
            color -> String.format("#%06X", color)
    );

    private static final Codec<Holder<NoiseDefinition>> NOISE_CODEC =
            RegistryFixedCodec.create(WorldbuilderRegistries.NOISE_DEFINITION_REGISTRY_KEY);

    public static final Codec<RegionConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(RegionConfig::name),
            COLOR_CODEC.fieldOf("color").forGetter(RegionConfig::color),
            NOISE_CODEC.fieldOf("noise").forGetter(RegionConfig::noise),
            RegionBiomesConfig.CODEC.fieldOf("biomes").forGetter(RegionConfig::biomes)
    ).apply(inst, RegionConfig::new));
}

