package ru.varep.worldbuilder.img.noise.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NoiseDefinition(
        Type type,
        double frequency,
        int octaves,
        double lacunarity,
        double gain,
        int seedOffset
) {
    public enum Type {
        FBM,
        PERLIN,
        SIMPLEX
    }

    public static final Codec<Type> TYPE_CODEC = Codec.STRING.xmap(
            s -> switch (s.toLowerCase()) {
                case "fbm" -> Type.FBM;
                case "perlin" -> Type.PERLIN;
                case "simplex" -> Type.SIMPLEX;
                default -> throw new IllegalArgumentException("[WORLDBUILDER] Unknown noise type: " + s);
            },
            t -> switch (t) {
                case FBM -> "fbm";
                case PERLIN -> "perlin";
                case SIMPLEX -> "simplex";
            }
    );

    public static final Codec<NoiseDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPE_CODEC.fieldOf("type").forGetter(NoiseDefinition::type),
            Codec.DOUBLE.optionalFieldOf("frequency", 0.01D).forGetter(NoiseDefinition::frequency),
            Codec.INT.optionalFieldOf("octaves", 4).forGetter(NoiseDefinition::octaves),
            Codec.DOUBLE.optionalFieldOf("lacunarity", 2.0D).forGetter(NoiseDefinition::lacunarity),
            Codec.DOUBLE.optionalFieldOf("gain", 0.5D).forGetter(NoiseDefinition::gain),
            Codec.INT.optionalFieldOf("seed_offset", 0).forGetter(NoiseDefinition::seedOffset)
    ).apply(instance, NoiseDefinition::new));
}
