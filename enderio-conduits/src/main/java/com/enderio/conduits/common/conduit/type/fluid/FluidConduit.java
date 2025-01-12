package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.filter.FluidStackFilter;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FluidConduit(ResourceLocation texture, Component description, int transferRatePerTick,
        boolean isMultiFluid) implements Conduit<FluidConduit, FluidConduitConnectionConfig> {

    public static final MapCodec<FluidConduit> CODEC = RecordCodecBuilder
            .mapCodec(
                    builder -> builder
                            .group(ResourceLocation.CODEC.fieldOf("texture").forGetter(FluidConduit::texture),
                                    ComponentSerialization.CODEC.fieldOf("description")
                                            .forGetter(FluidConduit::description),
                                    Codec.INT.fieldOf("transfer_rate").forGetter(FluidConduit::transferRatePerTick),
                                    Codec.BOOL.fieldOf("is_multi_fluid").forGetter(FluidConduit::isMultiFluid))
                            .apply(builder, FluidConduit::new));

    private static final FluidConduitTicker TICKER = new FluidConduitTicker();

    @Override
    public ConduitType<FluidConduit> type() {
        return ConduitTypes.FLUID.get();
    }

    @Override
    public FluidConduitTicker getTicker() {
        return TICKER;
    }

    @Override
    public boolean canBeInSameBundle(Holder<Conduit<?, ?>> otherConduit) {
        return !(otherConduit.value() instanceof FluidConduit);
    }

    @Override
    public boolean canBeReplacedBy(Holder<Conduit<?, ?>> otherConduit) {
        if (!(otherConduit.value() instanceof FluidConduit otherFluidConduit)) {
            return false;
        }

        return compareTo(otherFluidConduit) < 0;
    }

    @Override
    public boolean hasServerConnectionChecks() {
        return !isMultiFluid();
    }

    @Override
    public boolean canConnectConduits(ConduitNode selfNode, ConduitNode otherNode) {
        if (!isMultiFluid()) {
            return true;
        }

        // Ensure the networks are not locked to different fluids before connecting.
        var selfNetwork = selfNode.getNetwork();
        var otherNetwork = otherNode.getNetwork();

        // If one network does not yet exist, then we're good to connect.
        if (selfNetwork == null || otherNetwork == null) {
            return true;
        }

        var selfContext = selfNetwork.getContext(FluidConduitNetworkContext.TYPE);
        var otherContext = otherNetwork.getContext(FluidConduitNetworkContext.TYPE);

        if (selfContext == null || otherContext == null) {
            return true;
        }

        if (selfContext.lockedFluid().isSame(Fluids.EMPTY) || otherContext.lockedFluid().isSame(Fluids.EMPTY)) {
            return true;
        }

        return selfContext.lockedFluid().isSame(otherContext.lockedFluid());
    }

    @Override
    public boolean canConnectToBlock(Level level, BlockPos conduitPos, Direction direction) {
        IFluidHandler capability = level.getCapability(Capabilities.FluidHandler.BLOCK, conduitPos.relative(direction),
                direction.getOpposite());
        return capability != null;
    }

    @Override
    public boolean canApplyFilter(SlotType slotType, ResourceFilter resourceFilter) {
        return resourceFilter instanceof FluidStackFilter;
    }

    @Override
    public ConnectionConfigType<FluidConduitConnectionConfig> connectionConfigType() {
        return FluidConduitConnectionConfig.TYPE;
    }

    @Override
    public FluidConduitConnectionConfig convertConnection(boolean isInsert, boolean isExtract, DyeColor inputChannel,
            DyeColor outputChannel, RedstoneControl redstoneControl, DyeColor redstoneChannel) {
        return new FluidConduitConnectionConfig(isInsert, inputChannel, isExtract, outputChannel, redstoneControl,
                redstoneChannel);
    }

    @Override
    public void copyLegacyData(ConduitNode node, ConduitDataAccessor legacyDataAccessor) {
        var legacyData = legacyDataAccessor.getData(ConduitTypes.Data.FLUID.get());
        if (legacyData == null) {
            return;
        }

        var context = Objects.requireNonNull(node.getNetwork()).getOrCreateContext(FluidConduitNetworkContext.TYPE);

        if (!context.lockedFluid().isSame(Fluids.EMPTY)) {
            return;
        }

        // Copy locked fluid from old data.
        context.setLockedFluid(legacyData.lockedFluid());
    }

    @Override
    public @Nullable CompoundTag getExtraGuiData(ConduitBundleReader conduitBundle, ConduitNode node, Direction side) {
        return getExtraWorldData(conduitBundle, node);
    }

    @Override
    @Nullable
    public CompoundTag getExtraWorldData(ConduitBundleReader conduitBundle, ConduitNode node) {
        if (node.getNetwork() == null) {
            return null;
        }

        var context = node.getNetwork().getContext(FluidConduitNetworkContext.TYPE);
        if (context == null) {
            return null;
        }

        if (context.lockedFluid().isSame(Fluids.EMPTY)) {
            return null;
        }

        var tag = new CompoundTag();
        tag.putString("LockedFluid", BuiltInRegistries.FLUID.getKey(context.lockedFluid()).toString());
        return tag;
    }

    @Override
    public void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder,
            TooltipFlag pTooltipFlag) {
        String transferLimitFormatted = String.format("%,d", transferRatePerTick());
        pTooltipAdder
                .accept(TooltipUtil.styledWithArgs(ConduitLang.FLUID_EFFECTIVE_RATE_TOOLTIP, transferLimitFormatted));

        if (isMultiFluid()) {
            pTooltipAdder.accept(ConduitLang.MULTI_FLUID_TOOLTIP);
        }

        if (pTooltipFlag.hasShiftDown()) {
            String rawRateFormatted = String.format("%,d",
                    (int) Math.ceil(transferRatePerTick() * (20.0 / graphTickRate())));
            pTooltipAdder.accept(TooltipUtil.styledWithArgs(ConduitLang.FLUID_RAW_RATE_TOOLTIP, rawRateFormatted));
        }
    }

    @Override
    public boolean hasAdvancedTooltip() {
        return true;
    }

    @Override
    public boolean showDebugTooltip() {
        return true;
    }

    @Override
    public int compareTo(@NotNull FluidConduit o) {
        if (isMultiFluid() && !o.isMultiFluid()) {
            return 1;
        }

        if (transferRatePerTick() < o.transferRatePerTick()) {
            return -1;
        } else if (transferRatePerTick() > o.transferRatePerTick()) {
            return 1;
        }

        return 0;
    }
}
