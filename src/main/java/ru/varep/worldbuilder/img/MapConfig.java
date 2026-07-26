package ru.varep.worldbuilder.img;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MapConfig(float min, float max, int scale) {
    public static final MapConfig DEFAULT = new MapConfig(-1.0F, 1.0F, 1);

    public static final Codec<MapConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("min", DEFAULT.min).forGetter(MapConfig::min),
            Codec.FLOAT.optionalFieldOf("max", DEFAULT.max).forGetter(MapConfig::max),
            Codec.INT.optionalFieldOf("scale", DEFAULT.scale).forGetter(MapConfig::scale)
    ).apply(inst, MapConfig::new));
}
