package net.thegrimsey.stoneholm.util;

//? if <1.21.4 {
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
//?}

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;

public class StructurePoolUtils {
    public static void appendPool(StructureTemplatePool primaryPool, StructureTemplatePool secondaryPool) {
        StructurePoolAccessor primaryPoolAccessor = (StructurePoolAccessor) primaryPool;
        StructurePoolAccessor secondaryPoolAccessor = (StructurePoolAccessor) secondaryPool;

        //? if <1.21.4 {
        ArrayList<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(primaryPoolAccessor.getRawTemplates());
        rawTemplates.addAll(secondaryPoolAccessor.getRawTemplates());
        primaryPoolAccessor.setRawTemplates(rawTemplates);
        //?}

        ObjectArrayList<StructurePoolElement> templates = new ObjectArrayList<>(primaryPoolAccessor.getTemplates());
        templates.addAll(secondaryPoolAccessor.getTemplates());
        primaryPoolAccessor.setTemplates(templates);
    }
}
