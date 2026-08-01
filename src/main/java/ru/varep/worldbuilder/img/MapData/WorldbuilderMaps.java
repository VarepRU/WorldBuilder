package ru.varep.worldbuilder.img.MapData;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class WorldbuilderMaps {

    private static volatile Map<ResourceLocation, MapData> maps = Map.of();

    public static @Nullable HeightMapData getHeight(ResourceLocation id) {
        MapData md = maps.get(id);
        return (md instanceof HeightMapData hm) ? hm : null;
    }

    public static @Nullable BiomeMapData getBiome(ResourceLocation id) {
        MapData md = maps.get(id);
        return (md instanceof BiomeMapData bm) ? bm : null;
    }

    public static void replaceAll(Map<ResourceLocation, MapData> newMaps) {
        maps = Map.copyOf(newMaps);
    }

    private WorldbuilderMaps() {}
}
