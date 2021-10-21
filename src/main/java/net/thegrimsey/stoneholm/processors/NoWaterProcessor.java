package net.thegrimsey.stoneholm.processors;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.thegrimsey.stoneholm.Stoneholm;

import javax.annotation.Nullable;

public class NoWaterProcessor extends StructureProcessor {
    public static final Codec<NoWaterProcessor> CODEC = Codec.unit(NoWaterProcessor::new);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings placeSettings, @Nullable StructureTemplate template) {
        ChunkAccess chunk = level.getChunk(structureBlockInfoWorld.pos);

        if(structureBlockInfoWorld.state.hasProperty(BlockStateProperties.WATERLOGGED) && !chunk.getFluidState(structureBlockInfoWorld.pos).isEmpty())
        {
            boolean waterlog = (structureBlockInfoLocal.state.hasProperty(BlockStateProperties.WATERLOGGED) && structureBlockInfoLocal.state.getValue(BlockStateProperties.WATERLOGGED));

            chunk.setBlockState(structureBlockInfoWorld.pos, structureBlockInfoWorld.state.rotate(placeSettings.getRotation()).setValue(BlockStateProperties.WATERLOGGED, waterlog), false);
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Stoneholm.NOWATER_PROCESSOR;
    }
}
