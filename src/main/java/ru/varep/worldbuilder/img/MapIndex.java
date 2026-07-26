package ru.varep.worldbuilder.img;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MapIndex(List<ResourceLocation> maps) {
    public static final Codec<MapIndex> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("maps").forGetter(MapIndex::maps)
    ).apply(inst, MapIndex::new));
}
