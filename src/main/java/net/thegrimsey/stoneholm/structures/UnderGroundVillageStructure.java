package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.thegrimsey.stoneholm.Stoneholm;

public class UnderGroundVillageStructure extends StructureFeature<NoneFeatureConfiguration> {
    public static ResourceLocation START_POOL = new ResourceLocation(Stoneholm.MODID, "start_pool");

    public UnderGroundVillageStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return UnderGroundVillageStructure.Start::new;
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    // Equivalent to shouldStartAt in Fabric.


    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long worldSeed, WorldgenRandom worldgenRandom, ChunkPos pos, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration, LevelHeightAccessor heightAccessor) {
        // We don't want to spawn too far above the sea level because then we may end up spawning pieces above ground.
        // Bit-shift chunkX & Y for theoretical performance improvements. It is unclear if this really matters, I believe the compiler should be intelligent enough to do this on it's own.
        int terrainHeight = chunkGenerator.getBaseHeight(pos.x << 4, pos.z << 4, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor);
        int maxHeight = chunkGenerator.getSeaLevel() + Stoneholm.CONFIG.VILLAGE_MAX_DISTANCE_ABOVE_SEALEVEL;

        return terrainHeight <= maxHeight;
    }

    public static class Start extends StructureStart<NoneFeatureConfiguration> {
        private static JigsawConfiguration structurePoolFeatureConfig = null;

        public Start(StructureFeature<NoneFeatureConfiguration> p_163595_, ChunkPos p_163596_, int p_163597_, long p_163598_) {
            super(p_163595_, p_163596_, p_163597_, p_163598_);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkPos chunkPos, Biome biome, NoneFeatureConfiguration featureConfiguration, LevelHeightAccessor heightAccessor) {
            // Turns the chunk coordinates into actual coordinates.
            int x = chunkPos.x << 4;
            int z = chunkPos.z << 4;

            // Position, we don't care about Y as we will just be placed on top on the terrain.
            BlockPos blockPos = new BlockPos(x, 0, z);

            if (structurePoolFeatureConfig == null)
                structurePoolFeatureConfig = new JigsawConfiguration(() -> registryAccess.registry(Registry.TEMPLATE_POOL_REGISTRY).get().get(START_POOL), Stoneholm.CONFIG.VILLAGE_SIZE);

            JigsawPlacement.addPieces(registryAccess, structurePoolFeatureConfig, PoolElementStructurePiece::new, chunkGenerator, structureManager, blockPos, this.pieces, this.random, false, true);

            //Move structure up 1 block to ensure the entrance doesn't have blocks in front of it.
            this.pieces.forEach(piece -> piece.move(0, 1, 0));

        }
    }
}
