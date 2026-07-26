package ru.varep.worldbuilder.img;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import ru.varep.worldbuilder.WorldbuilderMod;


import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = WorldbuilderMod.MODID)
public final class WorldbuilderEvents {

    //переработать

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
}
