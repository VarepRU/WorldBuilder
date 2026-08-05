package ru.varep.worldbuilder.img.noise.core;

import ru.varep.worldbuilder.img.noise.sampler.FbmNoiseSampler;
import ru.varep.worldbuilder.img.noise.sampler.PerlinFbmNoiseSampler;
import ru.varep.worldbuilder.img.noise.sampler.SimplexFbmNoiseSampler;

public final class NoiseSamplerFactory {
    private NoiseSamplerFactory() {}

    public static NoiseSampler create(NoiseDefinition def, long worldSeed) {
        long seed = worldSeed + def.seedOffset();

        return switch (def.type()) {
            case FBM -> new FbmNoiseSampler(
                    seed,
                    def.frequency(),
                    def.octaves(),
                    def.lacunarity(),
                    def.gain()
            );
            case PERLIN -> new PerlinFbmNoiseSampler(
                    seed,
                    def.frequency(),
                    def.octaves(),
                    def.lacunarity(),
                    def.gain()
            );
            case SIMPLEX -> new SimplexFbmNoiseSampler(
                    seed,
                    def.frequency(),
                    def.octaves(),
                    def.lacunarity(),
                    def.gain()
            );
        };
    }
}
