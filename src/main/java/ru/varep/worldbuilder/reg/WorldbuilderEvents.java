package ru.varep.worldbuilder.reg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.img.data.BiomeMapData;
import ru.varep.worldbuilder.img.data.HeightMapData;
import ru.varep.worldbuilder.img.data.RegionMapData;
import ru.varep.worldbuilder.img.data.WorldbuilderMaps;
import ru.varep.worldbuilder.img.WorldbuilderMapLoader;
import ru.varep.worldbuilder.img.noise.core.NoiseDefinition;
import ru.varep.worldbuilder.util.DebugTracker;
import ru.varep.worldbuilder.util.DebugTracking;

@EventBusSubscriber(modid = WorldbuilderMod.MODID)
public final class WorldbuilderEvents {

    //переработать //или и так норм? //TODO ПЕРЕРАБОТАТЬ

    @SubscribeEvent
    public static void onServerStartedEvent(ServerStartedEvent event) {
        WorldbuilderMapLoader.reload(event.getServer());
    }

    //Регистри для шумов

    @SubscribeEvent
    public static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                WorldbuilderRegistries.NOISE_DEFINITION_REGISTRY_KEY,
                NoiseDefinition.CODEC,
                NoiseDefinition.CODEC
        );
    }

    // дебаг трекер
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DebugTracker tracker = DebugTracking.get(player);
        if (tracker == null) {
            return;
        }

        ResourceLocation id = tracker.id();
        BlockPos pos = player.blockPosition();
        int x = pos.getX();
        int z = pos.getZ();

        switch (tracker.type()) {
            case HEIGHT -> {
                HeightMapData map = WorldbuilderMaps.getHeight(id);
                if (map == null) {
                    DebugTracking.remove(player);
                    player.displayClientMessage(Component.literal("Tracked height map disappeared: " + id), true);
                    return;
                }

                float value = map.sample(x, z);
                float fx = (float) x / (float) map.scale();
                float fz = (float) z / (float) map.scale();

                player.displayClientMessage(Component.literal(
                        id +
                                " | " + x + " , " + z +
                                " | " + roundX(fx,0) + " , " + roundX(fz,0) +
                                " | v=" + roundX(value,3) +
                                " | Δ " + roundX(map.min(),3) + " / " + roundX(map.max(),2)
                ), true);
            }

            case BIOME -> {
                BiomeMapData map = WorldbuilderMaps.getBiome(id);
                if (map == null) {
                    DebugTracking.remove(player);
                    player.displayClientMessage(Component.literal("Tracked biome map disappeared: " + id), true);
                    return;
                }

                int rgb = map.sampleRgb(x, z);
                ResourceLocation biome = map.sampleBiome(x, z);
                float fx = (float) x / (float) map.scale();
                float fz = (float) z / (float) map.scale();

                int color = rgb & 0xFFFFFF;

                player.displayClientMessage(Component.literal(
                        id +
                                " | " + x + " , " + z +
                                " | " + roundX(fx,0) + " , " + roundX(fz,0)  +
                                " | #" + String.format("%06X", rgb & 0xFFFFFF) +
                                " | " + biome
                ).withColor(color), true);
            }

            case REGION_BIOME -> {
                RegionMapData map = WorldbuilderMaps.getRegion(id);
                if (map == null) {
                    DebugTracking.remove(player);
                    player.displayClientMessage(Component.literal("Tracked biome map disappeared: " + id), true);
                    return;
                }
                int rid = map.getRegionId(x,z);

                String name = map.getRegionName(rid);
                int color = map.getRegionColor(rid);
                ResourceKey<Biome> biome = map.getBiome(x, z);
                String biomeId = biome == null ? "null" : biome.location().toString();

                float fx = (float) x / (float) map.scale();
                float fz = (float) z / (float) map.scale();


                player.displayClientMessage(Component.literal(
                        id +
                                " | " + x + " , " + z +
                                " | " + roundX(fx,0) + " , " + roundX(fz,0)  +
                                " | " + name +
                                " | " + biomeId
                ).withColor(color), true);
            }
        }
    }
    private static String roundX(float v,int r) {
        return String.format(java.util.Locale.ROOT, "%."+ r +"f", v);
    }
}

