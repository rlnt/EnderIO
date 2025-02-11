package com.enderio.conduits.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.EnderIOConduits;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ConduitLang {

    // region Conduit Types

    public static final Component ENERGY_CONDUIT = addTranslation("item", EnderIO.loc("conduit.energy"),
            "Energy Conduit");
    public static final Component ENHANCED_ENERGY_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.enhanced_energy"), "Enhanced Energy Conduit");
    public static final Component ENDER_ENERGY_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_energy"),
            "Ender Energy Conduit");
    public static final Component REDSTONE_CONDUIT = addTranslation("item", EnderIO.loc("conduit.redstone"),
            "Redstone Conduit");
    public static final Component FLUID_CONDUIT = addTranslation("item", EnderIO.loc("conduit.fluid"),
            "Fluid Conduit");
    public static final Component PRESSURIZED_FLUID_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.pressurized_fluid"), "Pressurized Fluid Conduit");
    public static final Component ENDER_FLUID_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_fluid"),
            "Ender Fluid Conduit");
    public static final Component ITEM_CONDUIT = addTranslation("item", EnderIO.loc("conduit.item"),
            "Item Conduit");
    public static final Component ENHANCED_ITEM_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.enhanced_item"), "Enhanced Item Conduit");
    public static final Component ENDER_ITEM_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_item"),
            "Ender Item Conduit");

    // endregion

    public static final MutableComponent GRAPH_TICK_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.debug.tick_rate"), "Graph Ticks: %s/sec");

    public static final MutableComponent ENERGY_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.energy.rate"), "Max Output %s \u00B5I/t");

    public static final MutableComponent FLUID_RAW_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.raw_rate"), "Rate: %s mB/graph tick");
    public static final MutableComponent FLUID_EFFECTIVE_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.effective_rate"), "Effective Rate: %s mB/t");

    public static final Component MULTI_FLUID_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.multi"), "Allows multiple fluids to be transported on the same line");

    public static final MutableComponent ITEM_RAW_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.item.raw_rate"), "Rate: %s Items/graph tick");
    public static final MutableComponent ITEM_EFFECTIVE_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.item.effective_rate"), "Effective Rate: %s Items/sec");

    public static final Component CONDUIT_INSERT = addTranslation("gui", EnderIO.loc("conduit.insert"), "Insert");
    public static final Component CONDUIT_EXTRACT = addTranslation("gui", EnderIO.loc("conduit.extract"),
            "Extract");

    public static final MutableComponent TRANSPARENT_FACADE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit_facade.transparent"),
            "Transparent: Hides conduits when painted with a translucent block");
    public static final MutableComponent BLAST_RESIST_FACADE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit_facade.blast_resist"), "Hardened: Resists breaking and explosions");

    private static MutableComponent addTranslation(String prefix, ResourceLocation id, String translation) {
        return EnderIOConduits.REGILITE.addTranslation(prefix, id, translation);
    }

    private static MutableComponent addTranslation(String prefix, ResourceLocation path, String name,
            String translation) {
        return EnderIOConduits.REGILITE.addTranslation(prefix,
                ResourceLocation.fromNamespaceAndPath(path.getNamespace(), path.getPath() + "/" + name), translation);
    }

    public static void register() {
    }
}
