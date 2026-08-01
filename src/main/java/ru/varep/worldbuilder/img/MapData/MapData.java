package ru.varep.worldbuilder.img.MapData;

public sealed interface MapData permits HeightMapData, BiomeMapData {
    int width();
    int height();
    int scale();
}
