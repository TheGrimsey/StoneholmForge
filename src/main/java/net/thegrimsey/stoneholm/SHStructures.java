package net.thegrimsey.stoneholm;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thegrimsey.stoneholm.structures.DeepslateUnderGroundVillageStructure;
import net.thegrimsey.stoneholm.structures.NoWaterProcessor;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;
import net.thegrimsey.stoneholm.structures.WindowProcessor;

public class SHStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_TYPE, Stoneholm.MODID);

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS =
        DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Stoneholm.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<UnderGroundVillageStructure>> UNDERGROUND_VILLAGE =
        STRUCTURE_TYPES.register("underground_village", () -> () -> UnderGroundVillageStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<DeepslateUnderGroundVillageStructure>> DEEPSLATE_UNDERGROUND_VILLAGE =
        STRUCTURE_TYPES.register("deepslate_underground_village", () -> () -> DeepslateUnderGroundVillageStructure.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<NoWaterProcessor>> NOWATER_PROCESSOR =
        STRUCTURE_PROCESSORS.register("nowater_processor", () -> () -> NoWaterProcessor.CODEC);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<WindowProcessor>> WINDOW_PROCESSOR =
        STRUCTURE_PROCESSORS.register("window_processor", () -> () -> WindowProcessor.CODEC);
}
