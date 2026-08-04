package ru.varep.worldbuilder.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import ru.varep.worldbuilder.img.MapData.BiomeMapData;
import ru.varep.worldbuilder.img.MapData.HeightMapData;
import ru.varep.worldbuilder.img.MapData.MapData;
import ru.varep.worldbuilder.img.MapData.WorldbuilderMaps;

import java.util.Comparator;

import java.util.concurrent.CompletableFuture;

public final class DebugCommand {

    //я написал дебаг, чтобы разобраться с одной проблемой. только после написания дебага обнаружил, что дело было не в моде, а в моих кривых руках, что поставили лишнюю запятую в json

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wbdebug")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("list")
                                .executes(ctx -> executeList(ctx.getSource())))

                        .then(Commands.literal("stop")
                                .executes(ctx -> executeStop(ctx.getSource())))

                        .then(Commands.literal("track")
                                .then(Commands.literal("height")
                                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                                .suggests(DebugCommand::suggestHeightMaps)
                                                .executes(ctx -> executeTrackHeight(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "id")
                                                ))))
                                .then(Commands.literal("biome")
                                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                                .suggests(DebugCommand::suggestBiomeMaps)
                                                .executes(ctx -> executeTrackBiome(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "id")
                                                )))))
        );
    }

    private static int executeList(CommandSourceStack source) {
        var ids = WorldbuilderMaps.ids();

        if (ids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No maps loaded."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Loaded maps: " + ids.size()), false);

        ids.stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(id -> {
                    MapData data = WorldbuilderMaps.get(id);
                    String type =
                            data instanceof HeightMapData ? "height" :
                                    data instanceof BiomeMapData ? "biome" :
                                            "unknown";

                    source.sendSuccess(() -> Component.literal("- " + id + " [" + type + "]"), false);
                });

        return 1;
    }

    private static int executeTrackHeight(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (!(WorldbuilderMaps.get(id) instanceof HeightMapData)) {
            source.sendFailure(Component.literal("Height map not found: " + id));
            return 0;
        }

        DebugTracking.set(player, new DebugTracker(DebugTracker.MapType.HEIGHT, id));
        player.displayClientMessage(Component.literal("Tracking height map: " + id), true);
        return 1;
    }

    private static int executeTrackBiome(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (!(WorldbuilderMaps.get(id) instanceof BiomeMapData)) {
            source.sendFailure(Component.literal("Biome map not found: " + id));
            return 0;
        }

        DebugTracking.set(player, new DebugTracker(DebugTracker.MapType.BIOME, id));
        player.displayClientMessage(Component.literal("Tracking biome map: " + id), true);
        return 1;
    }

    private static int executeStop(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DebugTracking.remove(player);
        player.displayClientMessage(Component.literal("Debug tracking disabled"), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestHeightMaps(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        WorldbuilderMaps.ids().stream()
                .filter(id -> WorldbuilderMaps.get(id) instanceof HeightMapData)
                .map(ResourceLocation::toString)
                .sorted()
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBiomeMaps(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        WorldbuilderMaps.ids().stream()
                .filter(id -> WorldbuilderMaps.get(id) instanceof BiomeMapData)
                .map(ResourceLocation::toString)
                .sorted()
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

}




