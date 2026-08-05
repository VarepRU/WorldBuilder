package ru.varep.worldbuilder.reg;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import ru.varep.worldbuilder.WorldbuilderMod;
import ru.varep.worldbuilder.img.noise.core.NoiseDefinition;

public final class WorldbuilderRegistries {
    private WorldbuilderRegistries() {}

    public static final ResourceKey<Registry<NoiseDefinition>> NOISE_DEFINITION_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(WorldbuilderMod.MODID, "noise")
            );
}
