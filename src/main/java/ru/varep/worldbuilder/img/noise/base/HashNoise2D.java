package ru.varep.worldbuilder.img.noise.base;

public final class HashNoise2D implements BaseNoise2D {
    private final long seed;

    public HashNoise2D(long seed) {
        this.seed = seed;
    }

    @Override
    public double sample(double x, double z) {
        int xi = fastFloor(x);
        int zi = fastFloor(z);

        double tx = x - xi;
        double tz = z - zi;

        double v00 = hashToUnit(xi, zi);
        double v10 = hashToUnit(xi + 1, zi);
        double v01 = hashToUnit(xi, zi + 1);
        double v11 = hashToUnit(xi + 1, zi + 1);

        double sx = smooth(tx);
        double sz = smooth(tz);

        double ix0 = lerp(v00, v10, sx);
        double ix1 = lerp(v01, v11, sx);

        return lerp(ix0, ix1, sz);
    }

    private double hashToUnit(int x, int z) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= z * 0xC2B2AE3D27D4EB4FL;
        h ^= (h >>> 27);
        h *= 0x94D049BB133111EBL;
        h ^= (h >>> 31);

        // [0,1]
        double d = (h & 0x1FFFFFFFFFFFFFL) / (double) 0x1FFFFFFFFFFFFFL;
        // [-1,1]
        return d * 2.0 - 1.0;
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static int fastFloor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }
}
