package com.enderio.conduits.common.redstone;

import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitNetworkContext;
import com.enderio.conduits.common.init.ConduitComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class RedstoneANDFilter extends DoubleRedstoneChannel implements RedstoneInsertFilter {

    public RedstoneANDFilter(ItemStack stack) {
        super(stack, ConduitComponents.REDSTONE_AND_FILTER);
    }

    @Override
    public int getOutputSignal(RedstoneConduitNetworkContext context, DyeColor control) {
        boolean b = context.isActive(getFirstChannel()) && context.isActive(getSecondChannel());
        return b ? 15 : 0;
    }
}
