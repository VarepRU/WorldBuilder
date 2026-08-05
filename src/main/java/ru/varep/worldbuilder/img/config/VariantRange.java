package ru.varep.worldbuilder.img.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VariantRange(float min, float max) {
    public static final Codec<VariantRange> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("min").forGetter(VariantRange::min),
            Codec.FLOAT.fieldOf("max").forGetter(VariantRange::max)
    ).apply(inst, VariantRange::new));
}
