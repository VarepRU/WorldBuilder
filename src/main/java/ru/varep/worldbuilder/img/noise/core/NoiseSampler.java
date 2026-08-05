package ru.varep.worldbuilder.img.noise.core;

@FunctionalInterface
public interface NoiseSampler {
    double sample(int x, int z);
}