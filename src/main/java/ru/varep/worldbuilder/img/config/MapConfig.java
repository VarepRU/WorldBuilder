package ru.varep.worldbuilder.img.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;


import java.util.*;

public record MapConfig(float min,
                        float max,
                        int scale,
                        Mode mode,
                        NavigableMap<Integer, Float> steps,
                        Map<Integer, ResourceLocation> biomeMap,
                        List<RegionConfig> regions) {

    public enum Mode { GRADIENT, STEPS, BIOME, REGIONS }

    public static final MapConfig DEFAULT = new MapConfig(
            -1.0F,
            1.0F,
            1,
            Mode.GRADIENT,
            new TreeMap<>(),
            new HashMap<>(),
            List.of());

    private static final Codec<NavigableMap<Integer, Float>> STEPS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).xmap(map -> {
                var out = new TreeMap<Integer, Float>();
                for (var e : map.entrySet()) {
                    int k = Integer.parseInt(e.getKey());
                    if (k < 0 || k > 255) continue;
                    out.put(k, e.getValue());
                }
                return out;
            }, nav -> {
                var out = new HashMap<String, Float>();
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
                                if (s.equalsIgnoreCase("regions")) return Mode.REGIONS;
                                return Mode.GRADIENT;
                            },
                            m -> switch (m) {
                                case STEPS -> "steps";
                                case BIOME -> "biome";
                                case REGIONS -> "regions";
                                case GRADIENT -> "gradient";
                            })
                    .forGetter(MapConfig::mode),
            STEPS_CODEC.optionalFieldOf("steps", new TreeMap<>()).forGetter(MapConfig::steps),
            BIOME_CODEC.optionalFieldOf("biomes", new HashMap<>()).forGetter(MapConfig::biomeMap),
            RegionConfig.CODEC.listOf().optionalFieldOf("regions", List.of()).forGetter(MapConfig::regions)
    ).apply(inst, MapConfig::new));
}