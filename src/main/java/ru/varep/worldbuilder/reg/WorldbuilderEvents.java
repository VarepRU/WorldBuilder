package ru.varep.worldbuilder.reg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.img.MapData.BiomeMapData;
import ru.varep.worldbuilder.img.MapData.HeightMapData;
import ru.varep.worldbuilder.img.MapData.WorldbuilderMaps;
import ru.varep.worldbuilder.img.WorldbuilderMapLoader;
import ru.varep.worldbuilder.util.DebugTracker;
import ru.varep.worldbuilder.util.DebugTracking;


import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = WorldbuilderMod.MODID)
public final class WorldbuilderEvents {

    //переработать //или и так норм?

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent e) {
        e.addListener((barrier, manager, prepProfiler, applyProfiler, backgroundExecutor, gameExecutor) ->
                CompletableFuture
                        .supplyAsync(() -> {
                            prepProfiler.startTick();
                            WorldbuilderMapLoader.reload(manager);
                            prepProfiler.endTick();
                            return null;
                        }, backgroundExecutor)
                        .thenCompose(barrier::wait)
                        .thenRunAsync(() -> {
                            applyProfiler.startTick();
                            applyProfiler.endTick();
                        }, gameExecutor)
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
        }
    }
    private static String roundX(float v,int r) {
        return String.format(java.util.Locale.ROOT, "%."+ r +"f", v);
    }
}

