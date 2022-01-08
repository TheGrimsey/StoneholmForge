package net.thegrimsey.stoneholm;

import com.google.common.collect.ImmutableMap;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

import java.util.HashMap;
import java.util.Map;

public class SHStructures {
    public static final DeferredRegister<StructureFeature<?>> STRUCTURE_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Stoneholm.MODID);

    public static final RegistryObject<StructureFeature<JigsawConfiguration>> UNDERGROUND_VILLAGE = STRUCTURE_DEFERRED_REGISTER.register("underground_village", () -> new UnderGroundVillageStructure(JigsawConfiguration.CODEC));

    public static void registerStructureFeatures() {
        StructureFeatureConfiguration structureConfig = new StructureFeatureConfiguration(Stoneholm.CONFIG.VILLAGE_SPACING, Stoneholm.CONFIG.VILLAGE_SEPARATION, 8699777);
        StructureFeature.STRUCTURES_REGISTRY.put(UNDERGROUND_VILLAGE.get().getRegistryName().toString(), UNDERGROUND_VILLAGE.get());

        StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
                .putAll(StructureSettings.DEFAULTS)
                .put(UNDERGROUND_VILLAGE.get(), structureConfig)
                .build();

        BuiltinRegistries.NOISE_GENERATOR_SETTINGS.forEach(settings -> {
            Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.structureSettings().structureConfig();

            if (structureMap instanceof ImmutableMap) {
                Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
                tempMap.put(UNDERGROUND_VILLAGE.get(), structureConfig);
                settings.structureSettings().structureConfig = tempMap;
            } else {
                structureMap.put(UNDERGROUND_VILLAGE.get(), structureConfig);
            }
        });
    }
}
