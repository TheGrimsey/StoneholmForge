package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(Stoneholm.MODID)
public class Stoneholm {
    public static final String MODID = "stoneholm";
    public static final HashSet<Biome.Category> SPAWNABLE_BIOME_CATEGORIES =
            Stream.of(Biome.Category.FOREST, Biome.Category.JUNGLE, Biome.Category.DESERT, Biome.Category.PLAINS, Biome.Category.SAVANNA).collect(Collectors.toCollection(HashSet::new));

    public static SHConfig CONFIG;

    public Stoneholm() {
        // Register config file.
        AutoConfig.register(SHConfig.class, JanksonConfigSerializer::new);
        // Get config.
        CONFIG = AutoConfig.getConfigHolder(SHConfig.class).getConfig();

        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SHStructures.STRUCTURE_DEFERRED_REGISTER.register(modEventBus);
        modEventBus.addListener(this::setup);

        // For events that happen after initialization. This is probably going to be use a lot.
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        // The comments for BiomeLoadingEvent and StructureSpawnListGatherEvent says to do HIGH for additions.
        forgeBus.addListener(EventPriority.HIGH, this::biomeModification);

        if (CONFIG.disableVanillaVillages)
            forgeBus.addListener(EventPriority.NORMAL, this::removeVanillaVillages);
    }

    void setup(final FMLCommonSetupEvent event) {
        // Queue our structure registration to be done.
        event.enqueueWork(() -> {
            SHStructures.registerStructureFeatures();
            SHConfiguredStructures.registerConfiguredStructures();
        });
    }

    void biomeModification(final BiomeLoadingEvent event) {
        // Only register structure if biome is of the an acceptable category.
        if (SPAWNABLE_BIOME_CATEGORIES.contains(event.getCategory()))
            event.getGeneration().getStructures().add(() -> SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE);
    }

    void removeVanillaVillages(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();

            Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
            tempMap.keySet().remove(Structure.VILLAGE);
            serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
        }
    }
}
