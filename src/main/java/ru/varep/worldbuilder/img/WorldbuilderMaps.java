package ru.varep.worldbuilder.img;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class WorldbuilderMaps {

    public record MapData(
            int width,
            int height,
            int scale,
            float min,
            float max,
            float[] values
    ) {
        public float sampleWorld(int blockX, int blockZ) {
            int px = Math.floorDiv(blockX, scale);
            int pz = Math.floorDiv(blockZ, scale);

            if (px < 0 || pz < 0 || px >= width || pz >= height) return min;
            return values[pz * width + px];
        }
    }

    private static volatile Map<ResourceLocation, MapData> MAPS = Map.of();

    public static @Nullable MapData get(ResourceLocation id) {
        return MAPS.get(id);
    }

    public static void replaceAll(Map<ResourceLocation, MapData> maps) {
        MAPS = Map.copyOf(maps);
    }

    private WorldbuilderMaps() {}
}