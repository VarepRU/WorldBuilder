package ru.varep.worldbuilder.reg;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.util.DebugCommand;

@EventBusSubscriber(modid = WorldbuilderMod.MODID)
public final class WorldbuilderCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());
    }

}
