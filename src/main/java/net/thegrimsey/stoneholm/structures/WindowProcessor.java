package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.thegrimsey.stoneholm.SHStructures;
import org.jetbrains.annotations.Nullable;

//? if >=26.2 {
/*public class WindowProcessor implements StructureProcessor {*/
//?} else {
public class WindowProcessor extends StructureProcessor {
//?}
    public static final MapCodec<WindowProcessor> CODEC = MapCodec.unit(new WindowProcessor());

    @Nullable
    @Override
    //? if >=26.2 {
    /*public StructureTemplate.StructureBlockInfo process(LevelReader world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings data, @Nullable StructureTemplate template) {*/
    //?} else {
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings data) {
    //?}
        //? if >=26.2 {
        /*if (structureBlockInfoLocal.state().is(Blocks.CONCRETE.pink())) {*/
        //?} else {
        if (structureBlockInfoLocal.state().is(Blocks.PINK_CONCRETE)) {
        //?}
            BlockPos worldPos = structureBlockInfoWorld.pos();
            ChunkAccess chunk = world.getChunk(worldPos);

            boolean north = chunk.getBlockState(worldPos.relative(Direction.NORTH)).isAir();
            boolean south = chunk.getBlockState(worldPos.relative(Direction.SOUTH)).isAir();
            boolean east = chunk.getBlockState(worldPos.relative(Direction.EAST)).isAir();
            boolean west = chunk.getBlockState(worldPos.relative(Direction.WEST)).isAir();

            boolean shouldCreate = north && south || east && west;

            if (shouldCreate) {
                return new StructureTemplate.StructureBlockInfo(worldPos, Blocks.IRON_BARS.defaultBlockState(), null);
            } else {
                return new StructureTemplate.StructureBlockInfo(worldPos, Blocks.COBBLESTONE.defaultBlockState(), null);
            }
        }

        return structureBlockInfoWorld;
    }

    @Override
    //? if >=26.2 {
    /*public MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }*/
    //?} else {
    protected StructureProcessorType<?> getType() {
        return SHStructures.WINDOW_PROCESSOR.get();
    }
    //?}
}
