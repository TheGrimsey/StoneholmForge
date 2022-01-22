package net.thegrimsey.stoneholm.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thegrimsey.stoneholm.Stoneholm;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class StoneholmGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    public static Optional<PieceGenerator<JigsawConfiguration>> generate(PieceGeneratorSupplier.Context<JigsawConfiguration> inContext, PieceFactory pieceFactory, BlockPos pos) {
        int size = Stoneholm.CONFIG.VILLAGE_SIZE;
        if (size <= 0)
            return Optional.empty();

        RegistryAccess registryManager = inContext.registryAccess();
        Registry<StructureTemplatePool> registry = registryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        StructureTemplatePool structurePool = registry.get(UnderGroundVillageStructure.START_POOL);

        WorldgenRandom chunkRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        chunkRandom.setLargeFeatureSeed(inContext.seed(), inContext.chunkPos().x, inContext.chunkPos().z);

        StructurePoolElement startingElement = structurePool.getRandomTemplate(chunkRandom);
        if (startingElement == EmptyPoolElement.INSTANCE)
            return Optional.empty();

        ChunkGenerator chunkGenerator = inContext.chunkGenerator();
        StructureManager structureManager = inContext.structureManager();
        LevelHeightAccessor heightLimitView = inContext.heightAccessor();
        Predicate<Biome> biomePredicate = inContext.validBiome();
        StructureFeature.bootstrap();

        Rotation blockRotation = Rotation.getRandom(chunkRandom);
        PoolElementStructurePiece poolStructurePiece = pieceFactory.create(structureManager, startingElement, pos, startingElement.getGroundLevelDelta(), blockRotation, startingElement.getBoundingBox(structureManager, pos, blockRotation));
        BoundingBox pieceBoundingBox = poolStructurePiece.getBoundingBox();

        int centerX = (pieceBoundingBox.maxX() + pieceBoundingBox.minX()) / 2;
        int centerZ = (pieceBoundingBox.maxZ() + pieceBoundingBox.minZ()) / 2;
        int y = pos.getY() + chunkGenerator.getFirstFreeHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

        if (!biomePredicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(centerX), QuartPos.fromBlock(y), QuartPos.fromBlock(centerZ))))
            return Optional.empty();

        int yOffset = pieceBoundingBox.minY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.move(0, y - yOffset, 0);

        return Optional.of((structurePiecesCollector, context) -> {
            ArrayList<PoolElementStructurePiece> list = Lists.newArrayList(poolStructurePiece);

            AABB box = new AABB(centerX - 80, y - 80, centerZ - 80, centerX + 80 + 1, y + 80 + 1, centerZ + 80 + 1);
            StoneholmStructurePoolGenerator structurePoolGenerator = new StoneholmStructurePoolGenerator(registry, size, pieceFactory, chunkGenerator, structureManager, list, chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, new MutableObject<>(Shapes.join(Shapes.create(box), Shapes.create(AABB.of(pieceBoundingBox)), BooleanOp.ONLY_FIRST)), 0, null));

            // Go through all structure pieces in the project.
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                StoneholmShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, shapedPoolStructurePiece.sourceBlockPos, heightLimitView);
            }
            list.forEach(structurePiecesCollector::addPiece);
        });
    }

    public interface PieceFactory {
        PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class StoneholmStructurePoolGenerator {
        final Registry<StructureTemplatePool> registry;
        final int maxSize;
        final PieceFactory pieceFactory;
        final ChunkGenerator chunkGenerator;
        final StructureManager structureManager;
        final List<? super PoolElementStructurePiece> children;
        final Random random;
        final Deque<StoneholmShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        final StructureTemplatePool fallback_down;
        final StructureTemplatePool fallback_side;
        final StructureTemplatePool end_cap;

        // Terrible hack. Ignore these pools when doing terrainchecks.
        static final HashSet<ResourceLocation> terrainCheckIgnoredPools = new HashSet<>(Arrays.asList(
                new ResourceLocation(Stoneholm.MODID, "bee"),
                new ResourceLocation(Stoneholm.MODID, "deco_blocks"),
                new ResourceLocation(Stoneholm.MODID, "deco_coverings"),
                new ResourceLocation(Stoneholm.MODID, "deco_wallpapers"),
                new ResourceLocation(Stoneholm.MODID, "iron_golem"),
                new ResourceLocation(Stoneholm.MODID, "villagers"),
                new ResourceLocation(Stoneholm.MODID, "armor_stands")
        ));

        StoneholmStructurePoolGenerator(Registry<StructureTemplatePool> registry, int maxSize, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolElementStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            // TODO: Eventually move fallback pools somewhere else.
            fallback_down = registry.get(new ResourceLocation(Stoneholm.MODID, "fallback_down_pool"));
            fallback_side = registry.get(new ResourceLocation(Stoneholm.MODID, "fallback_side_pool"));
            end_cap = registry.get(new ResourceLocation(Stoneholm.MODID, "end"));
        }

        void generatePiece(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceStructureBlockPos, LevelHeightAccessor world) {
            StructurePoolElement structurePoolElement = piece.getElement();
            BlockPos sourcePos = piece.getPosition();
            Rotation sourceRotation = piece.getRotation();
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BoundingBox sourceBoundingBox = piece.getBoundingBox();
            int boundsMinY = sourceBoundingBox.minY();

            BlockPos sourceBlock = sourcePos.offset(sourceStructureBlockPos == null ? BlockPos.ZERO : sourceStructureBlockPos);

            // For every structure block in the piece.
            for (StructureTemplate.StructureBlockInfo structureBlock : structurePoolElement.getShuffledJigsawBlocks(this.structureManager, sourcePos, sourceRotation, this.random)) {
                if(sourceBlock.equals(structureBlock.pos))
                    continue;

                MutableObject<VoxelShape> structureShape;
                Direction structureBlockFaceDirection = JigsawBlock.getFrontFacing(structureBlock.state);
                BlockPos structureBlockPosition = structureBlock.pos;
                BlockPos structureBlockAimPosition = structureBlockPosition.relative(structureBlockFaceDirection);

                // Get pool that structure block is targeting.
                ResourceLocation structureBlockTargetPoolId = new ResourceLocation(structureBlock.nbt.getString("pool"));
                Optional<StructureTemplatePool>  targetPool = this.registry.getOptional(structureBlockTargetPoolId);
                if (targetPool.isEmpty() || targetPool.get().size() == 0 && !Objects.equals(structureBlockTargetPoolId, Pools.EMPTY.getRegistryName())) {
                    //LOGGER.warn("Empty or non-existent pool: {}", structureBlockTargetPoolId);
                    continue;
                }

                boolean ignoredPool = terrainCheckIgnoredPools.contains(structureBlockTargetPoolId);

                // Get end cap pool for target pool.
                ResourceLocation terminatorPoolId = targetPool.get().getFallback();
                Optional<StructureTemplatePool> terminatorPool = this.registry.getOptional(terminatorPoolId);
                if (terminatorPool.isEmpty() || terminatorPool.get().size() == 0 && !Objects.equals(terminatorPoolId, Pools.EMPTY.getRegistryName())) {
                    //LOGGER.warn("Empty or non-existent fallback pool: {}", terminatorPoolId);
                    continue;
                }

                // Check if target position is inside current piece's bounding box.
                boolean containsPosition = sourceBoundingBox.isInside(structureBlockAimPosition);
                if (containsPosition) {
                    structureShape = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(Shapes.create(AABB.of(sourceBoundingBox)));
                    }
                } else {
                    structureShape = pieceShape;
                }

                // Get spawnable elements
                ArrayList<StructurePoolElement> possibleElementsToSpawn = Lists.newArrayList();
                if (currentSize < this.maxSize) {
                    possibleElementsToSpawn.addAll(targetPool.get().getShuffledTemplates(this.random)); // Add in pool elements if we haven't reached max size.
                }
                possibleElementsToSpawn.addAll(terminatorPool.get().getShuffledTemplates(this.random)); // Add in terminator elements.

                for (StructurePoolElement iteratedStructureElement : possibleElementsToSpawn) {
                    if (iteratedStructureElement == EmptyPoolElement.INSTANCE)
                        break;

                    boolean placed = tryPlacePiece(piece, currentSize, world, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, iteratedStructureElement, currentSize >= 2 && !ignoredPool);
                    if(placed)
                        break;
                }
            }
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolElementStructurePiece piece, int currentSize, LevelHeightAccessor world, int boundsMinY, StructureTemplate.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (Rotation randomizedRotation : Rotation.getShuffled(this.random)) {
                // Get all structure blocks in structure.
                List<StructureTemplate.StructureBlockInfo> structureBlocksInStructure = element.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (StructureTemplate.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!JigsawBlock.canAttach(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BoundingBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFrontFacing(structureBlock.state).getStepY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.minY();
                    BoundingBox offsetBoundingBox = iteratedStructureBoundingBox.move(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (Shapes.joinIsNotEmpty(structureShape.getValue(), Shapes.create(AABB.of(offsetBoundingBox).deflate(0.25)), BooleanOp.ONLY_SECOND))
                        continue;

                    // STONEHOLM CUSTOM: Skip if top of bounding box is above terrain. This is extremely hacky. Like, genuinely this is terrible.
                    if(doTerrainCheck && structureBlockFaceDirection != Direction.DOWN) {
                        int maxYBuffer = 3;
                        int maxY = offsetBoundingBox.maxY() + maxYBuffer;

                        boolean minCorner = maxY > chunkGenerator.getFirstFreeHeight(offsetBoundingBox.minX(), offsetBoundingBox.minZ(), Heightmap.Types.WORLD_SURFACE_WG, world);
                        boolean maxCorner = maxY > chunkGenerator.getFirstFreeHeight(offsetBoundingBox.maxX(), offsetBoundingBox.maxZ(), Heightmap.Types.WORLD_SURFACE_WG, world);
                        boolean minXmaxZ = maxY > chunkGenerator.getFirstFreeHeight(offsetBoundingBox.minX(), offsetBoundingBox.maxZ(), Heightmap.Types.WORLD_SURFACE_WG, world);
                        boolean maxXminZ = maxY > chunkGenerator.getFirstFreeHeight(offsetBoundingBox.maxX(), offsetBoundingBox.minZ(), Heightmap.Types.WORLD_SURFACE_WG, world);

                        int overTerrainCorners = (minCorner ? 1 : 0) + (minXmaxZ ? 1 : 0) + (maxCorner ? 1 : 0) + (maxXminZ ? 1 : 0);

                        if (overTerrainCorners > 1) {
                            element = end_cap.getRandomTemplate(random);

                            if (overTerrainCorners > 2) {
                                if(currentSize + 2 > maxSize)
                                    element = end_cap.getRandomTemplate(random);
                                else
                                    element = fallback_side.getRandomTemplate(random);
                            }

                            // If failing switch pool elements to fallback
                            return tryPlacePiece(piece, currentSize, boundsMinY, structureBlock, structureShape, structureBlockPosition, structureBlockAimPosition, element);
                        }
                    }
                    // END STONEHOLM CUSTOM.

                    StructureTemplatePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.offset(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(Shapes.join(structureShape.getValue(), Shapes.create(AABB.of(offsetBoundingBox)), BooleanOp.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolElementStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructureTemplatePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolElementStructurePiece piece, int currentSize, int boundsMinY, StructureTemplate.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (Rotation randomizedRotation : Rotation.getShuffled(this.random)) {
                // Get all structure blocks in structure.
                List<StructureTemplate.StructureBlockInfo> structureBlocksInStructure = element.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (StructureTemplate.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!JigsawBlock.canAttach(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BoundingBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFrontFacing(structureBlock.state).getStepY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.minY();
                    BoundingBox offsetBoundingBox = iteratedStructureBoundingBox.move(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (Shapes.joinIsNotEmpty(structureShape.getValue(), Shapes.create(AABB.of(offsetBoundingBox).deflate(0.25)), BooleanOp.ONLY_SECOND))
                        continue;

                    StructureTemplatePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.offset(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(Shapes.join(structureShape.getValue(), Shapes.create(AABB.of(offsetBoundingBox)), BooleanOp.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolElementStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructureTemplatePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }
    }

    record StoneholmShapedPoolStructurePiece(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceBlockPos) {}
}

