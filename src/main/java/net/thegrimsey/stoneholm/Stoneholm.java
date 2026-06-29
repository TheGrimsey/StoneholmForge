package net.thegrimsey.stoneholm;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
//? if >=26.1 {
/*import net.minecraft.resources.Identifier;*/
//?} else {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.thegrimsey.stoneholm.util.StructurePoolUtils;

@Mod(Stoneholm.MODID)
public class Stoneholm {
    public static final String MODID = "stoneholm";
    //? if >=26.1 {
    /*public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = id("underground_village");*/
    //?} else {
    public static final ResourceLocation UNDERGROUNDVILLAGE_IDENTIFIER = id("underground_village");
    //?}
    public static final int VILLAGE_SIZE = 25;

    //? if >=26.1 {
    /*public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MODID, path); }
    public static Identifier parseId(String id) { return Identifier.parse(id); }*/
    //?} else {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation parseId(String id) {
        return ResourceLocation.parse(id);
    }
    //?}

    public Stoneholm(IEventBus modEventBus) {
        SHStructures.STRUCTURE_TYPES.register(modEventBus);
        SHStructures.STRUCTURE_PROCESSORS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(ServerStartingEvent event) {
        //? if >=1.21.4 {
        /*Registry<StructureTemplatePool> poolRegistry = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);*/
        //?} else {
        Registry<StructureTemplatePool> poolRegistry = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        //?}

        if (ModList.get().isLoaded("morevillagers")) {
            StructureTemplatePool pointOfInterest = poolRegistry.getOptional(id("point_of_interest")).orElse(null);
            StructureTemplatePool mvPointOfInterest = poolRegistry.getOptional(id("addons/morevillagers/morevillagers_point_of_interest")).orElse(null);
            StructurePoolUtils.appendPool(pointOfInterest, mvPointOfInterest);

            StructureTemplatePool abandonedPoi = poolRegistry.getOptional(id("abandoned_point_of_interest")).orElse(null);
            StructureTemplatePool mvAbandonedPoi = poolRegistry.getOptional(id("addons/morevillagers/morevillagers_abandoned_point_of_interest")).orElse(null);
            StructurePoolUtils.appendPool(abandonedPoi, mvAbandonedPoi);
        }
    }
}
