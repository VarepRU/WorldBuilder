package ru.varep.worldbuilder.img.data;

public sealed interface MapData permits BiomeMapData, HeightMapData, RegionMapData {
    int width();
    int height();
    int scale();
}
