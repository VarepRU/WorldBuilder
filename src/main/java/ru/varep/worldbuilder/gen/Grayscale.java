package ru.varep.worldbuilder.gen;

import com.mojang.serialization.MapCodec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import ru.varep.worldbuilder.img.WorldbuilderMaps;

public final class Grayscale implements DensityFunction {

    public static final MapCodec<Grayscale> DATA_CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("map").forGetter(f -> f.mapId)
            ).apply(inst, Grayscale::new));

    public static final KeyDispatchDataCodec<Grayscale> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);
    private final ResourceLocation mapId;

    public Grayscale(ResourceLocation mapId) {
        this.mapId = mapId;
    }

    @Override
    public double compute(FunctionContext ctx) {
        var map = WorldbuilderMaps.getHeight(mapId);
        return map == null ? 0.0 : map.sample(ctx.blockX(), ctx.blockZ());
    }

    @Override
    public void fillArray(double[] out, ContextProvider contexts) {
        for (int i = 0; i < out.length; i++) {
            FunctionContext ctx = contexts.forIndex(i);
            out[i] = compute(ctx);
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Grayscale(mapId));
    }

    @Override
    public double minValue() {
        var map = WorldbuilderMaps.getHeight(mapId);
        return map == null ? 0.0 : map.min();
    }

    @Override
    public double maxValue() {
        var map = WorldbuilderMaps.getHeight(mapId);
        return map == null ? 0.0 : map.max();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
