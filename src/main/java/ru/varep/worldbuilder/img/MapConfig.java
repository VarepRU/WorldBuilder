package ru.varep.worldbuilder.img;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public record MapConfig(float min,
                        float max,
                        int scale,
                        Mode mode,
                        NavigableMap<Integer, Float> steps,
                        Map<Integer, ResourceLocation> biomeMap) {

    public enum Mode { GRADIENT, STEPS, BIOME }

    public static final MapConfig DEFAULT = new MapConfig(
            -1.0F,
            1.0F,
            1,
            Mode.GRADIENT,
            new TreeMap<>(),
            new HashMap<>());

    private static final Codec<java.util.NavigableMap<Integer, Float>> STEPS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(map -> {
                var out = new java.util.TreeMap<Integer, Float>();
                for (var e : map.entrySet()) {
                    int k = Integer.parseInt(e.getKey());
                    if (k < 0 || k > 255) continue;
                    out.put(k, e.getValue());
                }
                return out;
            }, nav -> {
                var out = new java.util.HashMap<String, Float>();
                for (var e : nav.entrySet()) out.put(Integer.toString(e.getKey()), e.getValue());
                return out;
            });

    private static final Codec<Map<Integer, ResourceLocation>> BIOME_CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).xmap(
                    map -> {
                        var out = new HashMap<Integer, ResourceLocation>();
                        for (var e : map.entrySet()) {
                            try {
                                int rgb = Integer.parseInt(e.getKey(), 16);
                                out.put(rgb, e.getValue());
                            } catch (NumberFormatException ignored) {}
                        }
                        return out;
                    },
                    map -> {
                        var out = new HashMap<String, ResourceLocation>();
                        for (var e : map.entrySet()) {
                            out.put(String.format("%06X", e.getKey()), e.getValue());
                        }
                        return out;
                    }
            );

    public static final Codec<MapConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("min", DEFAULT.min).forGetter(MapConfig::min),
            Codec.FLOAT.optionalFieldOf("max", DEFAULT.max).forGetter(MapConfig::max),
            Codec.INT.optionalFieldOf("scale", DEFAULT.scale).forGetter(MapConfig::scale),
            Codec.STRING.optionalFieldOf("mode", "gradient")
                    .xmap(s -> {
                                if (s == null) return Mode.GRADIENT;
                                if (s.equalsIgnoreCase("steps")) return Mode.STEPS;
                                if (s.equalsIgnoreCase("biome")) return Mode.BIOME;
                                return Mode.GRADIENT;
                            },
                            m -> switch (m) {
                                case STEPS -> "steps";
                                case BIOME -> "biome";
                                case GRADIENT -> "gradient";
                            })
                    .forGetter(MapConfig::mode),
            STEPS_CODEC.optionalFieldOf("steps", new TreeMap<>()).forGetter(MapConfig::steps),
            BIOME_CODEC.optionalFieldOf("biomes", new HashMap<>()).forGetter(MapConfig::biomeMap)
    ).apply(inst, MapConfig::new));
}