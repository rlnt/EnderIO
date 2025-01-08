package com.enderio.conduits.api.ticker;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface ConduitTicker<TConduit extends Conduit<TConduit>> {

    void tickGraph(ServerLevel level, TConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider);
}
