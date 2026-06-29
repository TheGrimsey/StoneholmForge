package net.thegrimsey.stoneholm.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.block.Rotation;
import net.thegrimsey.stoneholm.Stoneholm;

public class StructureCompat {
    //? if >=1.21.4 {
    /*public static BlockPos getPos(StructureTemplate.JigsawBlockInfo info) { return info.info().pos(); }
    public static BlockState getState(StructureTemplate.JigsawBlockInfo info) { return info.info().state(); }*/
    //?} else {
    public static BlockPos getPos(StructureTemplate.StructureBlockInfo info) { return info.pos(); }
    public static BlockState getState(StructureTemplate.StructureBlockInfo info) { return info.state(); }
    //?}

    //? if >=1.21.10 {
    /*public static ResourceLocation getPoolId(StructureTemplate.JigsawBlockInfo info) { return info.pool().location(); }*/
    //?} else if >=1.21.4 {
    /*public static ResourceLocation getPoolId(StructureTemplate.JigsawBlockInfo info) { return info.pool(); }*/
    //?} else {
    public static ResourceLocation getPoolId(StructureTemplate.StructureBlockInfo info) { return Stoneholm.parseId(info.nbt().getString("pool")); }
    //?}

    public static PoolElementStructurePiece makePiece(StructureTemplateManager manager, StructurePoolElement element, BlockPos pos, int groundLevelDelta, Rotation rotation, BoundingBox box) {
        return new PoolElementStructurePiece(manager, element, pos, groundLevelDelta, rotation, box, LiquidSettings.APPLY_WATERLOGGING);
    }
}
