package com.enderio.conduits.api.ticker;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

public interface LoadedAwareConduitTicker<TConduit extends Conduit<TConduit, ?>> extends ConduitTicker<TConduit> {

    @Override
    default void tickGraph(ServerLevel level, TConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {
        List<ConduitNode> nodeIdentifiers = graph.getNodes().stream().filter(ConduitNode::isLoaded).toList();

        tickGraph(level, conduit, nodeIdentifiers, graph, coloredRedstoneProvider);
    }

    void tickGraph(ServerLevel level, TConduit type, List<ConduitNode> loadedNodes, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider);
}
