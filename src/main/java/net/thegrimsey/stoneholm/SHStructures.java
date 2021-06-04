package net.thegrimsey.stoneholm;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

import java.util.HashMap;
import java.util.Map;

public class SHStructures {
    public static final DeferredRegister<Structure<?>> STRUCTURE_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Stoneholm.MODID);

    public static final RegistryObject<Structure<NoFeatureConfig>> UNDERGROUND_VILLAGE = STRUCTURE_DEFERRED_REGISTER.register("underground_village", () -> new UnderGroundVillageStructure(NoFeatureConfig.CODEC));

    public static void registerStructureFeatures() {
        StructureSeparationSettings structureConfig = new StructureSeparationSettings(Stoneholm.CONFIG.VILLAGE_SPACING, Stoneholm.CONFIG.VILLAGE_SEPARATION, 8699777);
        Structure.STRUCTURES_REGISTRY.put(UNDERGROUND_VILLAGE.get().getRegistryName().toString(), UNDERGROUND_VILLAGE.get());

        DimensionStructuresSettings.DEFAULTS = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                .putAll(DimensionStructuresSettings.DEFAULTS)
                .put(UNDERGROUND_VILLAGE.get(), structureConfig)
                .build();

        WorldGenRegistries.NOISE_GENERATOR_SETTINGS.forEach(settings -> {
            Map<Structure<?>, StructureSeparationSettings> structureMap = settings.structureSettings().structureConfig;

            if (structureMap instanceof ImmutableMap) {
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(UNDERGROUND_VILLAGE.get(), structureConfig);
                settings.structureSettings().structureConfig = tempMap;
            } else {
                structureMap.put(UNDERGROUND_VILLAGE.get(), structureConfig);
            }
        });
    }
}
