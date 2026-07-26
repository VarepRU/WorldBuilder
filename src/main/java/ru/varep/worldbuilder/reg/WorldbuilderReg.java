package ru.varep.worldbuilder.reg;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.gen.Grayscale;


public final class WorldbuilderReg {

    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, WorldbuilderMod.MODID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>> GRAYSCALE =
            DENSITY_FUNCTION_TYPES.register("grayscale_map", Grayscale.CODEC::codec);
}
