package ru.varep.worldbuilder.util;

import net.minecraft.resources.ResourceLocation;

public record DebugTracker(MapType type, ResourceLocation id) {
    public enum MapType {
        HEIGHT,
        BIOME
    }
}
