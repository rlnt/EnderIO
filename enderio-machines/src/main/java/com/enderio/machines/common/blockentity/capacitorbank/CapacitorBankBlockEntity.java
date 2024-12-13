package com.enderio.machines.common.blockentity.capacitorbank;

import com.enderio.base.api.capacitor.FixedScalable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.core.common.network.NetworkDataSlot;
import com.enderio.machines.common.blockentity.base.MultiConfigurable;
import com.enderio.machines.common.blockentity.base.PoweredMachineBlockEntity;
import com.enderio.machines.common.blockentity.multienergy.CapacityTier;
import com.enderio.machines.common.blockentity.sync.LargeEnergyData;
import com.enderio.machines.common.energy.multi.MultiEnergyGraphContext;
import com.enderio.machines.common.energy.multi.MultiEnergyNetworkCapability;
import com.enderio.machines.common.energy.multi.MultiEnergyNetworkManager;
import com.enderio.machines.common.energy.multi.MultiEnergyNode;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.io.energy.ILargeMachineEnergyStorage;
import com.enderio.machines.common.io.energy.MachineEnergyStorage;
import com.enderio.machines.common.menu.CapacitorBankMenu;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CapacitorBankBlockEntity extends PoweredMachineBlockEntity implements MultiConfigurable, MultiEnergyNode {

    public final CapacityTier tier;

    private long addedEnergy = 0;
    private long removedEnergy = 0;
    public static final int AVERAGE_IO_OVER_X_TICKS = 10;

    private @Nullable Graph<MultiEnergyGraphContext> graph;

    private static final String DISPLAY_MODES = "displaymodes";

    private final Map<Direction, DisplayMode> displayModes = Util.make(() -> {
       Map<Direction, DisplayMode> map = new EnumMap<>(Direction.class);
       for (Direction direction: new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
           map.put(direction, DisplayMode.NONE);
       }

       return map;
    });

    public static final NetworkDataSlot.CodecType<Map<Direction, DisplayMode>> DISPLAY_MODE_MAP_DATA_SLOT_TYPE =
        NetworkDataSlot.CodecType.createMap(Direction.CODEC, DisplayMode.CODEC, Direction.STREAM_CODEC.cast(), DisplayMode.STREAM_CODEC.cast());

    public CapacitorBankBlockEntity(BlockPos worldPosition, BlockState blockState, CapacitorTier tier) {
        super(EnergyIOMode.Both, new FixedScalable(tier::getStorageCapacity), new FixedScalable(tier::getStorageCapacity), MachineBlockEntities.CAPACITOR_BANKS.get(tier).get(), worldPosition, blockState);
        this.tier = tier;

        addDataSlot(NetworkDataSlot.LONG.create(() -> addedEnergy, data -> addedEnergy = data));
        addDataSlot(NetworkDataSlot.LONG.create(() -> removedEnergy, data -> removedEnergy = data));
        addDataSlot(DISPLAY_MODE_MAP_DATA_SLOT_TYPE.create(() -> displayModes, displayModes::putAll));
    }

    @Override
    public NetworkDataSlot<?> createEnergyDataSlot() {
        return LargeEnergyData.DATA_SLOT_TYPE.create(
            () -> LargeEnergyData.from((ILargeMachineEnergyStorage) getExposedEnergyStorage()),
            energyData -> clientEnergyStorage = energyData.toImmutableStorage());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new CapacitorBankMenu(pContainerId, this, pPlayerInventory);
    }

    // TODO: allow the exposed storage to be IEnergyStorage.
    @Override
    public @Nullable MachineEnergyStorage createExposedEnergyStorage() {
        return new MultiEnergyNetworkCapability(this, this, EnergyIOMode.Both);
    }

    @Override
    public void serverTick() {
        super.serverTick();
//        if (level.getGameTime() % AVERAGE_IO_OVER_X_TICKS == 0 && node.getWrapper().get().getLastResetTime() != level.getGameTime()) {
//            if (node.getGraph() != null) {
//                addedEnergy = 0;
//                removedEnergy = 0;
//                List<GraphObject<Mergeable.Dummy>> nodes = new ArrayList<>(node.getGraph().getObjects());
//                for (GraphObject<Mergeable.Dummy> object : nodes) {
//                    if (object instanceof MultiEnergyNode graphNode) {
//                        addedEnergy += graphNode.getWrapper().get().getAddedEnergy();
//                        removedEnergy += graphNode.getWrapper().get().getRemovedEnergy();
//                        graphNode.getWrapper().get().resetEnergyStats(level.getGameTime());
//                    }
//                }
//
//                //Sync it back to other capacitor bank in this graph, only one can do this calculation, because each node is reset at once
//                for (GraphObject<Mergeable.Dummy> object : nodes) {
//                    if (object instanceof MultiEnergyNode graphNode && level.getBlockEntity(graphNode.pos) instanceof CapacitorBankBlockEntity capacitorBank) {
//                        capacitorBank.addedEnergy = addedEnergy;
//                        capacitorBank.removedEnergy = removedEnergy;
//                    }
//                }
//            }
//        }
//
//        if (level.getGameTime() % 200 == hashCode() % 200 && node.getGraph() != null && List.copyOf(node.getGraph().getObjects()).indexOf(node) == 0) {
//            long cumulativeEnergy = 0;
//            for (GraphObject<Mergeable.Dummy> object : node.getGraph().getObjects()) {
//                if (object instanceof MultiEnergyNode otherNode) {
//                    cumulativeEnergy += otherNode.getInternal().get().getEnergyStored();
//                }
//            }
//
//            int energyPerNode = (int)(cumulativeEnergy / node.getGraph().getObjects().size());
//
//            for (GraphObject<Mergeable.Dummy> object : node.getGraph().getObjects()) {
//                if (object instanceof MultiEnergyNode otherNode) {
//                    ((MachineEnergyStorage)(otherNode.getInternal().get())).setEnergyStored(Math.min(energyPerNode, (int)Math.min(cumulativeEnergy, Integer.MAX_VALUE)));
//                    cumulativeEnergy-=energyPerNode;
//                }
//            }
//
//            int remainingEnergy = (int)cumulativeEnergy;
//            if (remainingEnergy <= 0) {
//                return;
//            }
//
//            for (GraphObject<Mergeable.Dummy> object : node.getGraph().getObjects()) {
//                if (object instanceof MultiEnergyNode otherNode) {
//                    int received = otherNode.getInternal().get().receiveEnergy(remainingEnergy, false);
//                    remainingEnergy-=received;
//                    if (remainingEnergy <= 0) {
//                        return;
//                    }
//                }
//            }
//        }
    }

    @Override
    protected boolean isActive() {
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        // Save energy before local energy storage is saved
        if (!level.isClientSide) {
            MultiEnergyNetworkManager.saveNodes(this);
        }

        super.saveAdditional(pTag, lookupProvider);
        pTag.put(DISPLAY_MODES, saveDisplayModes());
    }

    public CompoundTag saveDisplayModes() {
        CompoundTag nbt = new CompoundTag();
        for (var entry : displayModes.entrySet()) {
            nbt.putInt(entry.getKey().getName(), entry.getValue().ordinal());
        }

        return nbt;
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(pTag, lookupProvider);
        if (pTag.contains(DISPLAY_MODES, Tag.TAG_COMPOUND)) {
            loadDisplayModes(pTag.getCompound(DISPLAY_MODES));
        }
    }

    public void loadDisplayModes(CompoundTag nbt) {
        displayModes.clear();
        for (String key : nbt.getAllKeys()) {
            @Nullable
            Direction dir = Direction.byName(key);
            if (dir != null) {
                displayModes.put(dir, DisplayMode.values()[nbt.getInt(key)]);
            }
        }
    }

    @Override
    protected boolean shouldPushEnergyTo(Direction direction) {
        if (graph == null) {
            return true;
        }

        if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CapacitorBankBlockEntity capacitorBank) {
            return capacitorBank.getGraph() != graph;
        }

        return true;
    }

    @Override
    public void setRemoved() {
        if (!level.isClientSide()) {
            MultiEnergyNetworkManager.removeNode(this);
        } else {
            graph.remove(this);
        }

        super.setRemoved();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!level.isClientSide) {
            if (graph == null) {
                MultiEnergyNetworkManager.initNode(this, tier.getStorageCapacity());
            }

            for (Direction direction: Direction.values()) {
                if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CapacitorBankBlockEntity capacitor && capacitor.tier == tier) {
                    MultiEnergyNetworkManager.addNode(this, capacitor, tier.getStorageCapacity());
                }
            }
        } else {
            // TODO: A different strategy for getting all of the locations for the IOconfig widget, for now we just use the graph.
            if (graph == null) {
                Graph.integrate(this, List.of());
            }

            for (Direction direction: Direction.values()) {
                if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CapacitorBankBlockEntity capacitor && capacitor.tier == tier) {
                    Graph.connect(this, capacitor);
                }
            }
        }
    }

    @Override
    public List<BlockPos> getConfigurables() {
        // TODO: Do we want to have a client-side graph for just this?
        if (graph == null) {
            return List.of();
        }

        List<BlockPos> positions = new ArrayList<>();
        for (GraphObject<MultiEnergyGraphContext> object : graph.getObjects()) {
            if (object instanceof CapacitorBankBlockEntity otherNode) {
                positions.add(otherNode.getBlockPos());
            }
        }
        return positions;
    }

    public boolean onShiftRightClick(Direction direction, Player player) {
        if (direction.getAxis().getPlane() == Direction.Plane.VERTICAL) {
            return false;
        }

        if (player.getMainHandItem().getItem() instanceof BlockItem || player.getOffhandItem().getItem() instanceof BlockItem) {
            return false;
        }

        if (player.getMainHandItem().is(EIOTags.Items.WRENCH)) {
            return false;
        }

        displayModes.put(direction, DisplayMode.values()[(displayModes.get(direction).ordinal()+1)%DisplayMode.values().length]);
        return true;
    }

    public long getAddedEnergy() {
        return addedEnergy / AVERAGE_IO_OVER_X_TICKS;
    }

    public long getRemovedEnergy() {
        return removedEnergy / AVERAGE_IO_OVER_X_TICKS;
    }

    public DisplayMode getDisplayMode(Direction direction) {
        if (getLevel() == null || !Block.shouldRenderFace(getBlockState(), getLevel(), worldPosition, direction, worldPosition.relative(direction))) {
            return DisplayMode.NONE;
        }

        return displayModes.get(direction);
    }

    public void setDisplayMode(Direction direction, DisplayMode mode) {
        displayModes.put(direction, mode);
    }

    @Override
    public int getLocalEnergyStored() {
        return getEnergyStorage().getEnergyStored();
    }

    @Override
    public void setLocalEnergyStored(int energyStored) {
        if (!level.isClientSide) {
            getEnergyStorage().setEnergyStored(energyStored);
        }
    }

    @Nullable
    @Override
    public Graph<MultiEnergyGraphContext> getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Graph<MultiEnergyGraphContext> graph) {
        this.graph = graph;
    }
}
