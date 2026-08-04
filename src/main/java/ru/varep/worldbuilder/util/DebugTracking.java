package ru.varep.worldbuilder.util;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DebugTracking {

    private static final Map<UUID, DebugTracker> TRACKING = new ConcurrentHashMap<>();

    public static void set(ServerPlayer player, DebugTracker tracker) {
        TRACKING.put(player.getUUID(), tracker);
    }

    public static DebugTracker get(ServerPlayer player) {
        return TRACKING.get(player.getUUID());
    }

    public static void remove(ServerPlayer player) {
        TRACKING.remove(player.getUUID());
    }

    public static boolean isTracking(ServerPlayer player) {
        return TRACKING.containsKey(player.getUUID());
    }

    private DebugTracking() {


    }
}
