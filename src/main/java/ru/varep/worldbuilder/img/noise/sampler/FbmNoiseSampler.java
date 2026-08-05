package ru.varep.worldbuilder.img.noise.sampler;

import ru.varep.worldbuilder.img.noise.base.HashNoise2D;
import ru.varep.worldbuilder.img.noise.core.NoiseSampler;

public final class FbmNoiseSampler implements NoiseSampler {
    private final HashNoise2D base;
    private final double frequency;
    private final int octaves;
    private final double lacunarity;
    private final double gain;

    public FbmNoiseSampler(long seed, double frequency, int octaves, double lacunarity, double gain) {
        this.base = new HashNoise2D(seed);
        this.frequency = frequency;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
    }

    @Override
    public double sample(int x, int z) {
        double fx = x * frequency;
        double fz = z * frequency;

        double amplitude = 1.0;
        double freqMul = 1.0;
        double sum = 0.0;
        double norm = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += base.sample(fx * freqMul, fz * freqMul) * amplitude;
            norm += amplitude;
            amplitude *= gain;
            freqMul *= lacunarity;
        }

        if (norm == 0.0) return 0.0;
        return sum / norm;
    }
}