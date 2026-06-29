package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.thegrimsey.stoneholm.SHStructures;

import java.util.Optional;

public class UnderGroundVillageStructure extends Structure {
    public static final MapCodec<UnderGroundVillageStructure> CODEC = simpleCodec(UnderGroundVillageStructure::new);

    static final Direction[] DIRECTIONS = new Direction[] {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST
    };
    static final BooleanProperty[] DIRECTION_PROPERTIES = new BooleanProperty[] {
        CrossCollisionBlock.NORTH,
        CrossCollisionBlock.SOUTH,
        CrossCollisionBlock.EAST,
        CrossCollisionBlock.WEST
    };

    public UnderGroundVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;
        BlockPos blockPos = new BlockPos(x, 0, z);
        return StoneholmGenerator.generate(context, blockPos, BlockSet.STONE_BRICKS);
    }

    static final net.minecraft.world.level.block.Block[] BARS = {
        Blocks.IRON_BARS,
        Blocks.GLASS_PANE,
        Blocks.WHITE_STAINED_GLASS_PANE,
        Blocks.ORANGE_STAINED_GLASS_PANE,
        Blocks.MAGENTA_STAINED_GLASS_PANE,
        Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
        Blocks.YELLOW_STAINED_GLASS_PANE,
        Blocks.LIME_STAINED_GLASS_PANE,
        Blocks.PINK_STAINED_GLASS_PANE,
        Blocks.GRAY_STAINED_GLASS_PANE,
        Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
        Blocks.CYAN_STAINED_GLASS_PANE,
        Blocks.PURPLE_STAINED_GLASS_PANE,
        Blocks.BLUE_STAINED_GLASS_PANE,
        Blocks.BROWN_STAINED_GLASS_PANE,
        Blocks.GREEN_STAINED_GLASS_PANE,
        Blocks.RED_STAINED_GLASS_PANE,
        Blocks.BLACK_STAINED_GLASS_PANE,
    };

    @Override
    public void afterPlace(WorldGenLevel world, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        net.minecraft.world.level.block.Block newBlock = BARS[random.nextInt(BARS.length)];

        pieces.pieces().forEach(structurePiece -> {
            BoundingBox boundingBox = structurePiece.getBoundingBox();
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
                for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                    for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                        blockPos.set(x, y, z);
                        BlockState blockState = world.getBlockState(blockPos);
                        if (blockState.is(Blocks.PINK_CONCRETE)) {
                            if (
                                world.getBlockState(blockPos.relative(Direction.NORTH)).isAir() && world.getBlockState(blockPos.relative(Direction.SOUTH)).isAir()
                                    || world.getBlockState(blockPos.relative(Direction.EAST)).isAir() && world.getBlockState(blockPos.relative(Direction.WEST)).isAir()
                            ) {
                                BlockState newState = newBlock.defaultBlockState();

                                for (int i = 0; i < DIRECTIONS.length; i++) {
                                    Direction direction = DIRECTIONS[i];
                                    BooleanProperty directionProperty = DIRECTION_PROPERTIES[i];

                                    BlockPos adjacentPos = blockPos.relative(direction);
                                    BlockState adjacentState = world.getBlockState(adjacentPos);

                                    if (((IronBarsBlock) Blocks.IRON_BARS).attachsTo(adjacentState, adjacentState.isFaceSturdy(world, adjacentPos, direction.getOpposite()))) {
                                        newState = newState.setValue(directionProperty, true);
                                    }
                                }

                                world.setBlock(blockPos, newState, net.minecraft.world.level.block.Block.UPDATE_ALL);
                            } else {
                                BlockState adjacentState = world.getBlockState(blockPos.relative(Direction.DOWN));
                                world.setBlock(blockPos, adjacentState.getBlock().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        });

        super.afterPlace(world, structureManager, chunkGenerator, random, box, chunkPos, pieces);
    }

    @Override
    public StructureType<?> type() {
        return SHStructures.UNDERGROUND_VILLAGE.get();
    }
}
