package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.thegrimsey.stoneholm.Stoneholm;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UnderGroundVillageStructure extends StructureFeature<JigsawConfiguration> {
    public static final ResourceLocation START_POOL = new ResourceLocation(Stoneholm.MODID, "start_pool");

    public UnderGroundVillageStructure(Codec<JigsawConfiguration> codec) {
        super(codec, UnderGroundVillageStructure::createPiecesGenerator);
    }

    @Override
    public GenerationStep.@NotNull Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    public static @NotNull Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, set Y to 1 to offset height up.
        BlockPos blockPos = new BlockPos(x, 1, z);

        return StoneholmGenerator.generate(context, PoolElementStructurePiece::new, blockPos);
    }
}
