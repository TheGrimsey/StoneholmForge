package net.thegrimsey.stoneholm;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class SHConfiguredStructures {
    public static ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> CONFIGURED_UNDERGROUND_VILLAGE = SHStructures.UNDERGROUND_VILLAGE.get().configured(FeatureConfiguration.NONE);

    public static void registerConfiguredStructures() {
        ResourceKey<Registry<ConfiguredStructureFeature<?, ?>>> registry = Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY;
        Registry.register(registry, new ResourceLocation(Stoneholm.MODID, "configured_underground_village"), CONFIGURED_UNDERGROUND_VILLAGE);

        FlatLevelGeneratorSettings.STRUCTURE_FEATURES.put(SHStructures.UNDERGROUND_VILLAGE.get(), CONFIGURED_UNDERGROUND_VILLAGE);
    }
}
