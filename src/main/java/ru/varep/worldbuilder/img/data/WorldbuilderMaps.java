package ru.varep.worldbuilder.img.data;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public final class WorldbuilderMaps {

    private static volatile Map<ResourceLocation, MapData> maps = Map.of();

    public static @Nullable MapData get(ResourceLocation id) {
        return maps.get(id);
    }

    public static @Nullable HeightMapData getHeight(ResourceLocation id) {
        MapData md = maps.get(id);
        return (md instanceof HeightMapData hm) ? hm : null;
    }

    public static @Nullable BiomeMapData getBiome(ResourceLocation id) {
        MapData md = maps.get(id);
        return (md instanceof BiomeMapData bm) ? bm : null;
    }

    public static @Nullable RegionMapData getRegion(ResourceLocation id) {
        MapData md = maps.get(id);
        return (md instanceof RegionMapData rm) ? rm : null;
    }

    public static void replaceAll(Map<ResourceLocation, MapData> newMaps) {
        maps = Map.copyOf(newMaps);
    }

    public static void setMaps(Map<ResourceLocation, MapData> newMaps) {
        maps = Map.copyOf(newMaps);
    }

    public static Set<ResourceLocation> ids() {
        return maps.keySet();
    }

    private WorldbuilderMaps() {}
}
