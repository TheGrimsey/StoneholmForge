package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.thegrimsey.stoneholm.SHStructures;

import java.util.Optional;

public class DeepslateUnderGroundVillageStructure extends UnderGroundVillageStructure {
    public static final MapCodec<DeepslateUnderGroundVillageStructure> CODEC = simpleCodec(DeepslateUnderGroundVillageStructure::new);

    public DeepslateUnderGroundVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        //? if >=26.1 {
        /*int x = context.chunkPos().x() << 4;
        int z = context.chunkPos().z() << 4;*/
        //?} else {
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;
        //?}
        return StoneholmGenerator.generate(context, new BlockPos(x, 0, z), BlockSet.DEEPSLATE);
    }

    @Override
    public StructureType<?> type() {
        return SHStructures.DEEPSLATE_UNDERGROUND_VILLAGE.get();
    }
}
