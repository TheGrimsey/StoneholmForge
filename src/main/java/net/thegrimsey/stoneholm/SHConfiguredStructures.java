package net.thegrimsey.stoneholm;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class SHConfiguredStructures {
    public static StructureFeature<?, ?> CONFIGURED_UNDERGROUND_VILLAGE = SHStructures.UNDERGROUND_VILLAGE.get().configured(IFeatureConfig.NONE);

    public static void registerConfiguredStructures() {
        Registry<StructureFeature<?,?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;
        Registry.register(registry, new ResourceLocation(Stoneholm.MODID, "configured_underground_village"), CONFIGURED_UNDERGROUND_VILLAGE);

        FlatGenerationSettings.STRUCTURE_FEATURES.put(SHStructures.UNDERGROUND_VILLAGE.get(), CONFIGURED_UNDERGROUND_VILLAGE);
    }
}
