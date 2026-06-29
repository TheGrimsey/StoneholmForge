package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.thegrimsey.stoneholm.SHStructures;
import org.jetbrains.annotations.Nullable;

public class NoWaterProcessor extends StructureProcessor {
    public static final MapCodec<NoWaterProcessor> CODEC = MapCodec.unit(new NoWaterProcessor());

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings data) {
        ChunkAccess chunk = world.getChunk(structureBlockInfoWorld.pos());

        if (structureBlockInfoWorld.state().hasProperty(BlockStateProperties.WATERLOGGED) && !chunk.getFluidState(structureBlockInfoWorld.pos()).isEmpty()) {
            boolean waterlog = structureBlockInfoLocal.state().hasProperty(BlockStateProperties.WATERLOGGED) && structureBlockInfoLocal.state().getValue(BlockStateProperties.WATERLOGGED);

            //? if >=1.21.10 {
            /*chunk.setBlockState(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().rotate(data.getRotation()).setValue(BlockStateProperties.WATERLOGGED, waterlog), 0);*/
            //?} else {
            chunk.setBlockState(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().rotate(data.getRotation()).setValue(BlockStateProperties.WATERLOGGED, waterlog), false);
            //?}
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return SHStructures.NOWATER_PROCESSOR.get();
    }
}
