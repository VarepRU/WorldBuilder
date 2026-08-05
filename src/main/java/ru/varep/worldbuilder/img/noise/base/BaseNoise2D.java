package ru.varep.worldbuilder.img.noise.base;

@FunctionalInterface
public interface BaseNoise2D {
    double sample(double x, double z);
}