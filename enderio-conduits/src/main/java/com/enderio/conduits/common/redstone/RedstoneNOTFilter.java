package com.enderio.conduits.common.redstone;

import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public class RedstoneNOTFilter implements RedstoneInsertFilter, RedstoneExtractFilter {

    public static final RedstoneNOTFilter INSTANCE = new RedstoneNOTFilter();

    private RedstoneNOTFilter() {
    }

    @Override
    public int getOutputSignal(RedstoneConduitData data, DyeColor control) {
        return data.isActive(control) ? 0 : 15;
    }

    @Override
    public int getInputSignal(Level level, BlockPos pos, Direction direction) {
        return level.getSignal(pos, direction) == 0 ? 15 : 0;
    }
}
