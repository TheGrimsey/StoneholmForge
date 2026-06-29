package net.thegrimsey.stoneholm.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thegrimsey.stoneholm.Stoneholm;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class StoneholmGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    static final ResourceLocation[] WALL_LIGHTING_POOLS = {
        Stoneholm.id("wall_lighting_lantern"),
        Stoneholm.id("wall_lighting_torch"),
    };

    static final ResourceLocation[] CORRIDORS = {
        Stoneholm.id("stone_bricks/corridors"),
        Stoneholm.id("deepslate/corridors")
    };
    static final ResourceLocation[] FUSILAGE = {
        Stoneholm.id("stone_bricks/fusilage"),
        Stoneholm.id("deepslate/fusilage")
    };
    static final ResourceLocation[] CISTERN_FUSILAGE = {
        Stoneholm.id("stone_bricks/cistern_fusilage"),
        Stoneholm.id("deepslate/cistern_fusilage")
    };
    static final ResourceLocation[] CISTERN = {
        Stoneholm.id("stone_bricks/cistern"),
        Stoneholm.id("deepslate/cistern")
    };
    static final ResourceLocation[] BEDROOM = {
        Stoneholm.id("stone_bricks/bedroom"),
        Stoneholm.id("deepslate/bedroom")
    };
    static final ResourceLocation[] COURTYARD = {
        Stoneholm.id("stone_bricks/courtyard"),
        Stoneholm.id("deepslate/courtyard")
    };
    static final ResourceLocation[] JOB = {
        Stoneholm.id("stone_bricks/job"),
        Stoneholm.id("deepslate/job")
    };
    static final ResourceLocation[] EASTER_EGGS = {
        Stoneholm.id("stone_bricks/easter_eggs"),
        Stoneholm.id("deepslate/easter_eggs")
    };
    static final ResourceLocation[] STAIRS = {
        Stoneholm.id("stone_bricks/stairs"),
        Stoneholm.id("deepslate/stairs")
    };
    static final ResourceLocation[] STAIRS_START = {
        Stoneholm.id("stone_bricks/stairs_start"),
        Stoneholm.id("deepslate/stairs_start")
    };
    static final ResourceLocation[] STAIRS_END = {
        Stoneholm.id("stone_bricks/stairs_end"),
        Stoneholm.id("deepslate/stairs_end")
    };
    static final ResourceLocation[] CLUTTER = {
        Stoneholm.id("stone_bricks/clutter"),
        Stoneholm.id("deepslate/clutter")
    };
    static final ResourceLocation[] END_CAP = {
        Stoneholm.id("stone_bricks/end_cap"),
        Stoneholm.id("deepslate/end_cap")
    };
    static final ResourceLocation[] START_POOLS = {
        Stoneholm.id("stone_bricks/start_pool"),
        Stoneholm.id("deepslate/start_pool")
    };

    static final double EXTENTS = 64.0;

    public static Optional<Structure.GenerationStub> generate(Structure.GenerationContext inContext, BlockPos pos, BlockSet blockSet) {
        int size = Stoneholm.CONFIG.VILLAGE_SIZE;
        if (size <= 0)
            return Optional.empty();

        //? if >=1.21.4 {
        /*Registry<StructureTemplatePool> registry = inContext.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);*/
        //?} else {
        Registry<StructureTemplatePool> registry = inContext.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        //?}
        StructureTemplatePool structurePool = registry.getOptional(START_POOLS[blockSet.id]).orElse(null);

        WorldgenRandom chunkRandom = new WorldgenRandom(inContext.random());
        chunkRandom.setLargeFeatureSeed(inContext.seed(), inContext.chunkPos().x, inContext.chunkPos().z);

        StructurePoolElement startingElement = structurePool.getRandomTemplate(chunkRandom);
        if (startingElement == EmptyPoolElement.INSTANCE)
            return Optional.empty();

        ChunkGenerator chunkGenerator = inContext.chunkGenerator();
        StructureTemplateManager structureManager = inContext.structureTemplateManager();
        LevelHeightAccessor heightLimitView = inContext.heightAccessor();

        Rotation blockRotation = Rotation.getRandom(chunkRandom);
        PoolElementStructurePiece poolStructurePiece = StructureCompat.makePiece(structureManager, startingElement, pos, startingElement.getGroundLevelDelta(), blockRotation, startingElement.getBoundingBox(structureManager, pos, blockRotation));
        BoundingBox pieceBoundingBox = poolStructurePiece.getBoundingBox();

        int centerX = (pieceBoundingBox.maxX() + pieceBoundingBox.minX()) / 2;
        int centerZ = (pieceBoundingBox.maxZ() + pieceBoundingBox.minZ()) / 2;
        int y = pos.getY() + chunkGenerator.getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView, inContext.randomState());

        int yOffset = pieceBoundingBox.minY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.move(0, y - yOffset, 0);

        //? if >=1.21.4 {
        /*AABB maxExtents = new AABB((double) centerX - EXTENTS, inContext.heightAccessor().getMinY(), (double) centerZ - EXTENTS,
                (double) centerX + EXTENTS, inContext.heightAccessor().getMaxY() + 1, (double) centerZ + EXTENTS);*/
        //?} else {
        AABB maxExtents = new AABB((double) centerX - EXTENTS, inContext.heightAccessor().getMinBuildHeight(), (double) centerZ - EXTENTS,
                (double) centerX + EXTENTS, inContext.heightAccessor().getMaxBuildHeight(), (double) centerZ + EXTENTS);
        //?}

        return Optional.of(new Structure.GenerationStub(new BlockPos(centerX, y, centerZ), (collector) -> {
            ArrayList<PoolElementStructurePiece> list = Lists.newArrayList(poolStructurePiece);

            AABB box = new AABB(centerX - 80, y - 80, centerZ - 80, centerX + 80 + 1, y + 80 + 1, centerZ + 80 + 1);
            StoneholmStructurePoolGenerator structurePoolGenerator = new StoneholmStructurePoolGenerator(registry, size, chunkGenerator, structureManager, list, chunkRandom, blockSet, maxExtents);
            structurePoolGenerator.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, new MutableObject<>(Shapes.join(Shapes.create(box), Shapes.create(AABB.of(pieceBoundingBox)), BooleanOp.ONLY_FIRST)), 0, null));

            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                StoneholmShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, shapedPoolStructurePiece.sourceBlockPos, heightLimitView, inContext.randomState());
            }
            list.forEach(collector::addPiece);
        }));
    }


    static final class StoneholmStructurePoolGenerator {
        final Registry<StructureTemplatePool> registry;
        final int maxSize;
        final ChunkGenerator chunkGenerator;
        final StructureTemplateManager structureManager;
        final List<? super PoolElementStructurePiece> children;
        final RandomSource random;
        final Deque<StoneholmShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        final StructureTemplatePool fallback_side;
        final StructureTemplatePool end_cap;

        final StructureTemplatePool wall_lighting;
        final StructureTemplatePool corridors;
        final StructureTemplatePool bedroom;
        final StructureTemplatePool courtyard;

        final StructureTemplatePool job;
        final StructureTemplatePool easter_eggs;
        final StructureTemplatePool stairs;
        final StructureTemplatePool stairs_end;
        final StructureTemplatePool stairs_start;

        final StructureTemplatePool clutter;
        final StructureTemplatePool cistern;
        final StructureTemplatePool cisternFusilagePool;
        final StructurePoolElement fusilage;
        final StructurePoolElement cisternFusilage;

        final AABB maxExtents;

        int yieldedCorridors = 0;
        int yieldedBedrooms = 0;
        int yieldedCourtyards = 0;
        int yieldedJobs = 0;
        int yieldedRooms = 0;

        static final HashSet<ResourceLocation> terrainCheckIgnoredPools;
        static {
            String[] base = { "bee", "deco_blocks", "deco_coverings", "deco_wallpapers", "iron_golem", "villagers", "armor_stands", "corridors", "clutter" };
            String[] themes = { "stone_bricks", "deepslate" };
            terrainCheckIgnoredPools = new HashSet<>(base.length * (themes.length + 1));
            for (String name : base) {
                terrainCheckIgnoredPools.add(Stoneholm.id(name));
                for (String theme : themes) {
                    terrainCheckIgnoredPools.add(Stoneholm.id(theme + "/" + name));
                }
            }
        }

        static final ResourceLocation WALL_LIGHTING = Stoneholm.id("wall_lighting");
        static final ResourceLocation CONNECTORS = Stoneholm.id("connectors");
        static final ResourceLocation STAIRS_ID = Stoneholm.id("stairs");
        static final ResourceLocation STAIRS_START_ID = Stoneholm.id("stairs_start");

        static final ResourceLocation CISTERN_ID = Stoneholm.id("cistern");
        static final ResourceLocation CISTERN_FUSILAGE_ID = Stoneholm.id("cistern_fusilage");
        static final ResourceLocation CLUTTER_ID = Stoneholm.id("clutter");

        static final HashSet<ResourceLocation> NO_FUSILAGE = new HashSet<>(Arrays.asList(
            WALL_LIGHTING,
            Stoneholm.id("misc_room"),
            Stoneholm.id("villager"),
            CISTERN_FUSILAGE_ID,
            CLUTTER_ID
        ));

        StoneholmStructurePoolGenerator(Registry<StructureTemplatePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureManager, List<? super PoolElementStructurePiece> children, WorldgenRandom random, BlockSet blockSet, AABB maxExtents) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;
            this.maxExtents = maxExtents;

            wall_lighting = registry.getOptional(WALL_LIGHTING_POOLS[random.nextInt(WALL_LIGHTING_POOLS.length)]).orElse(null);
            clutter = registry.getOptional(CLUTTER[blockSet.id]).orElse(null);
            corridors = registry.getOptional(CORRIDORS[blockSet.id]).orElse(null);
            cistern = registry.getOptional(CISTERN[blockSet.id]).orElse(null);
            cisternFusilagePool = registry.getOptional(CISTERN_FUSILAGE[blockSet.id]).orElse(null);
            fusilage = registry.getOptional(FUSILAGE[blockSet.id]).orElse(null).getRandomTemplate(random);
            cisternFusilage = cisternFusilagePool.getRandomTemplate(random);
            bedroom = registry.getOptional(BEDROOM[blockSet.id]).orElse(null);
            courtyard = registry.getOptional(COURTYARD[blockSet.id]).orElse(null);
            job = registry.getOptional(JOB[blockSet.id]).orElse(null);
            easter_eggs = registry.getOptional(EASTER_EGGS[blockSet.id]).orElse(null);

            stairs = registry.getOptional(STAIRS[blockSet.id]).orElse(null);
            stairs_start = registry.getOptional(STAIRS_START[blockSet.id]).orElse(null);
            stairs_end = registry.getOptional(STAIRS_END[blockSet.id]).orElse(null);

            end_cap = registry.getOptional(END_CAP[blockSet.id]).orElse(null);
            fallback_side = end_cap;
        }

        Optional<StructureTemplatePool> getPool(ResourceLocation id, ChunkGenerator chunkGenerator, LevelHeightAccessor world, RandomState randomState, BlockPos sourceConnector) {
            if (id.equals(WALL_LIGHTING)) {
                return Optional.of(wall_lighting);
            } else if (id.equals(CONNECTORS)) {
                yieldedRooms++;

                if (yieldedCorridors < 3) {
                    yieldedCorridors++;
                    return Optional.of(corridors);
                }
                float bedroomRatio = (float) yieldedBedrooms / (float) yieldedRooms;
                if (bedroomRatio < 0.2 || this.random.nextInt(100) < 20) {
                    yieldedBedrooms++;
                    return Optional.of(bedroom);
                }
                float jobRatio = (float) yieldedJobs / (float) yieldedRooms;
                if (jobRatio < 0.3 || this.random.nextInt(100) < 30) {
                    yieldedJobs++;
                    return Optional.of(job);
                }

                double courtyardChance = 0.85 * Math.pow(0.7, yieldedCourtyards);
                if (yieldedCourtyards < 2 && this.random.nextDouble() < courtyardChance) {
                    yieldedCourtyards++;
                    return Optional.of(courtyard);
                }

                if (this.random.nextDouble() < 0.03 && yieldedRooms >= 8) {
                    return Optional.of(easter_eggs);
                }

                yieldedCorridors++;
                return Optional.of(corridors);
            } else if (id.equals(STAIRS_ID)) {
                if (sourceConnector.getY() < chunkGenerator.getFirstOccupiedHeight(sourceConnector.getX(), sourceConnector.getZ(), Heightmap.Types.WORLD_SURFACE_WG, world, randomState) - 21 && this.random.nextDouble() < 0.9) {
                    return Optional.of(stairs_end);
                } else {
                    return Optional.of(stairs);
                }
            } else if (id.equals(STAIRS_START_ID)) {
                return Optional.of(stairs_start);
            } else if (id.equals(CISTERN_ID)) {
                return Optional.of(cistern);
            } else if (id.equals(CISTERN_FUSILAGE_ID)) {
                return Optional.of(cisternFusilagePool);
            } else if (id.equals(CLUTTER_ID)) {
                return Optional.of(clutter);
            } else {
                return this.registry.getOptional(id);
            }
        }

        void generatePiece(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceStructureBlockPos, LevelHeightAccessor world, RandomState randomState) {
            StructurePoolElement structurePoolElement = piece.getElement();
            BlockPos sourcePos = piece.getPosition();
            Rotation sourceRotation = piece.getRotation();
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BoundingBox sourceBoundingBox = piece.getBoundingBox();
            int boundsMinY = sourceBoundingBox.minY();

            BlockPos sourceBlock = sourcePos.offset(sourceStructureBlockPos == null ? BlockPos.ZERO : sourceStructureBlockPos);

            for (var structureBlock : structurePoolElement.getShuffledJigsawBlocks(this.structureManager, sourcePos, sourceRotation, this.random)) {
                BlockPos structureBlockPosition = StructureCompat.getPos(structureBlock);
                if (sourceBlock.equals(structureBlockPosition))
                    continue;
                ResourceLocation structureBlockTargetPoolId = StructureCompat.getPoolId(structureBlock);
                boolean noFusilage = NO_FUSILAGE.contains(structureBlockTargetPoolId);
                int offset = noFusilage ? 1 : 2;

                MutableObject<VoxelShape> structureShape;
                Direction structureBlockFaceDirection = JigsawBlock.getFrontFacing(StructureCompat.getState(structureBlock));
                BlockPos structureBlockAimPosition = structureBlockPosition.relative(structureBlockFaceDirection, offset);

                Optional<StructureTemplatePool> targetPool = this.getPool(structureBlockTargetPoolId, chunkGenerator, world, randomState, structureBlockPosition);
                if (targetPool.isEmpty() || targetPool.get().size() == 0 && !Objects.equals(structureBlockTargetPoolId, Pools.EMPTY.location())) {
                    LOGGER.warn("Empty or non-existent pool: {}", structureBlockTargetPoolId);
                    continue;
                }

                boolean ignoredPool = terrainCheckIgnoredPools.contains(structureBlockTargetPoolId);

                Holder<StructureTemplatePool> entry = targetPool.get().getFallback();
                StructureTemplatePool fallbackPool = entry.value();
                if (fallbackPool.size() == 0 && !entry.is(Pools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", entry.unwrapKey().get().location());
                    continue;
                }

                boolean containsPosition = sourceBoundingBox.isInside(structureBlockAimPosition);
                if (containsPosition) {
                    structureShape = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(Shapes.create(AABB.of(sourceBoundingBox)));
                    }
                } else {
                    structureShape = pieceShape;
                }

                if (structureBlockTargetPoolId.equals(CISTERN_ID)) {
                    tryPlacePiece(piece, this.maxSize, world, randomState, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockPosition.relative(structureBlockFaceDirection), this.cisternFusilage, false);
                } else if (!noFusilage) {
                    tryPlacePiece(piece, this.maxSize, world, randomState, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockPosition.relative(structureBlockFaceDirection), this.fusilage, false);
                }

                boolean doTerrainCheck = currentSize >= 2 && !ignoredPool;
                boolean placed = false;
                if (currentSize < this.maxSize) {
                    for (StructurePoolElement element : targetPool.get().getShuffledTemplates(this.random)) {
                        if (element == EmptyPoolElement.INSTANCE) break;
                        placed = tryPlacePiece(piece, currentSize, world, randomState, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, element, doTerrainCheck);
                        if (placed) break;
                    }
                }
                if (!placed) {
                    for (StructurePoolElement element : fallbackPool.getShuffledTemplates(this.random)) {
                        if (element == EmptyPoolElement.INSTANCE) break;
                        if (tryPlacePiece(piece, currentSize, world, randomState, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, element, doTerrainCheck)) break;
                    }
                }
            }
        }

        boolean tryPlacePiece(PoolElementStructurePiece piece, int currentSize, LevelHeightAccessor world, RandomState randomState, int boundsMinY,
                //? if >=1.21.4 {
                /*StructureTemplate.JigsawBlockInfo*/
                //?} else {
                StructureTemplate.StructureBlockInfo
                //?}
                structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (Rotation randomizedRotation : Rotation.getShuffled(this.random)) {
                var structureBlocksInStructure = element.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, randomizedRotation, this.random);

                for (var structureBlockInfo : structureBlocksInStructure) {
                    if (!JigsawBlock.canAttach(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = StructureCompat.getPos(structureBlockInfo);
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BoundingBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + structureBlockFaceDirection.getStepY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.minY();
                    BoundingBox offsetBoundingBox = iteratedStructureBoundingBox.moved(0, pieceYOffset, 0);
                    AABB contractedBox = AABB.of(offsetBoundingBox).deflate(0.25);
                    VoxelShape offsetVoxelShape = Shapes.create(contractedBox);

                    if (Shapes.joinIsNotEmpty(structureShape.getValue(), offsetVoxelShape, BooleanOp.ONLY_SECOND))
                        continue;

                    boolean entirelyContained = contractedBox.minX >= this.maxExtents.minX && contractedBox.maxX <= this.maxExtents.maxX && contractedBox.minZ >= this.maxExtents.minZ && contractedBox.maxZ <= this.maxExtents.maxZ;
                    if (!entirelyContained)
                        continue;

                    if (doTerrainCheck && structureBlockFaceDirection != Direction.DOWN) {
                        int maxY = offsetBoundingBox.maxY() + 3;
                        int overTerrainCorners = 0;
                        if (maxY > chunkGenerator.getFirstOccupiedHeight(offsetBoundingBox.minX(), offsetBoundingBox.minZ(), Heightmap.Types.WORLD_SURFACE_WG, world, randomState)) overTerrainCorners++;
                        if (maxY > chunkGenerator.getFirstOccupiedHeight(offsetBoundingBox.maxX(), offsetBoundingBox.maxZ(), Heightmap.Types.WORLD_SURFACE_WG, world, randomState)) overTerrainCorners++;
                        if (maxY > chunkGenerator.getFirstOccupiedHeight(offsetBoundingBox.minX(), offsetBoundingBox.maxZ(), Heightmap.Types.WORLD_SURFACE_WG, world, randomState)) overTerrainCorners++;
                        if (overTerrainCorners < 3 && maxY > chunkGenerator.getFirstOccupiedHeight(offsetBoundingBox.maxX(), offsetBoundingBox.minZ(), Heightmap.Types.WORLD_SURFACE_WG, world, randomState)) overTerrainCorners++;

                        if (overTerrainCorners > 2) {
                            element = (overTerrainCorners > 2 && currentSize + 2 <= maxSize)
                                ? fallback_side.getRandomTemplate(random)
                                : end_cap.getRandomTemplate(random);
                            return tryPlacePiece(piece, currentSize, boundsMinY, structureBlock, structureShape, structureBlockPosition, structureBlockAimPosition, element);
                        }
                    }

                    StructureTemplatePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.offset(0, pieceYOffset, 0);

                    structureShape.setValue(Shapes.join(structureShape.getValue(), offsetVoxelShape, BooleanOp.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolElementStructurePiece poolStructurePiece = StructureCompat.makePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructureTemplatePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize)
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }

        boolean tryPlacePiece(PoolElementStructurePiece piece, int currentSize, int boundsMinY,
                //? if >=1.21.4 {
                /*StructureTemplate.JigsawBlockInfo*/
                //?} else {
                StructureTemplate.StructureBlockInfo
                //?}
                structureBlock, MutableObject<VoxelShape> structureShape, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();
            int facingOffsetY = JigsawBlock.getFrontFacing(StructureCompat.getState(structureBlock)).getStepY();

            for (Rotation randomizedRotation : Rotation.getShuffled(this.random)) {
                var structureBlocksInStructure = element.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, randomizedRotation, this.random);

                for (var structureBlockInfo : structureBlocksInStructure) {
                    if (JigsawBlock.canAttach(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = StructureCompat.getPos(structureBlockInfo);
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BoundingBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + facingOffsetY;
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.minY();
                    BoundingBox offsetBoundingBox = iteratedStructureBoundingBox.moved(0, pieceYOffset, 0);
                    VoxelShape offsetVoxelShape = Shapes.create(AABB.of(offsetBoundingBox).deflate(0.25));

                    if (Shapes.joinIsNotEmpty(structureShape.getValue(), offsetVoxelShape, BooleanOp.ONLY_SECOND))
                        continue;

                    StructureTemplatePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.offset(0, pieceYOffset, 0);

                    structureShape.setValue(Shapes.join(structureShape.getValue(), offsetVoxelShape, BooleanOp.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolElementStructurePiece poolStructurePiece = StructureCompat.makePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructureTemplatePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize)
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }
    }

    record StoneholmShapedPoolStructurePiece(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceBlockPos) {}
}
