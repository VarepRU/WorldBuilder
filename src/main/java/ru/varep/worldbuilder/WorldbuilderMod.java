package ru.varep.worldbuilder;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import ru.varep.worldbuilder.reg.WorldbuilderReg;



@Mod(WorldbuilderMod.MODID)
public final class WorldbuilderMod {
    public static final String MODID = "worldbuilder";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WorldbuilderMod(IEventBus modEventBus) {
        WorldbuilderReg.DENSITY_FUNCTION_TYPES.register(modEventBus);
        WorldbuilderReg.BIOME_SOURCE_TYPES.register(modEventBus);
    }
}
