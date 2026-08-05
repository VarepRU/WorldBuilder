package ru.varep.worldbuilder.img.noise.base;

import java.util.Random;

public final class PerlinNoise2D {
    private static final int[] GRAD_X = { 1, -1, 1, -1, 1, -1, 1, -1 };
    private static final int[] GRAD_Z = { 1, 1, -1, -1, 0, 0, 0, 0 };

    private final int[] perm = new int[512];

    public PerlinNoise2D(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        Random random = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }

        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    public double sample(double x, double z) {
        int xi = fastFloor(x) & 255;
        int zi = fastFloor(z) & 255;

        double xf = x - fastFloor(x);
        double zf = z - fastFloor(z);

        double u = fade(xf);
        double v = fade(zf);

        int aa = perm[perm[xi] + zi];
        int ab = perm[perm[xi] + zi + 1];
        int ba = perm[perm[xi + 1] + zi];
        int bb = perm[perm[xi + 1] + zi + 1];

        double x1 = lerp(
                grad(aa, xf, zf),
                grad(ba, xf - 1.0, zf),
                u
        );
        double x2 = lerp(
                grad(ab, xf, zf - 1.0),
                grad(bb, xf - 1.0, zf - 1.0),
                u
        );

        return lerp(x1, x2, v);
    }

    private static double grad(int hash, double x, double z) {
        int h = hash & 7;
        return GRAD_X[h] * x + GRAD_Z[h] * z;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static int fastFloor(double x) {
        int i = (int) x;
        return x < i ? i - 1 : i;
    }
}
