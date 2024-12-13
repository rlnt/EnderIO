package com.enderio.machines.common.energy.multi;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.energy.ILargeMachineEnergyStorage;
import com.enderio.machines.common.io.energy.MachineEnergyStorage;
import dev.gigaherz.graph3.GraphObject;
import dev.gigaherz.graph3.Mergeable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

// TODO: This could be separated from MachineEnergyStorage.
public class MultiEnergyNetworkCapability extends MachineEnergyStorage implements ILargeMachineEnergyStorage {

    private final MultiEnergyNode node;

    public MultiEnergyNetworkCapability(MultiEnergyNode node, IOConfigurable config, EnergyIOMode ioMode) {
        super(config, ioMode, () -> 0, () -> Integer.MAX_VALUE);
        this.node = node;
    }

    @Override
    public long getLargeEnergyStored() {
        return MultiEnergyNetworkManager.getEnergyStored(node);
    }

    @Override
    public long getLargeMaxEnergyStored() {
        return MultiEnergyNetworkManager.getMaxEnergyStored(node);
    }

    @Override
    public int getEnergyStored() {
        return (int)Math.min(getLargeEnergyStored(), Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int)Math.min(getLargeMaxEnergyStored(), Integer.MAX_VALUE);
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        long energyBefore = getLargeEnergyStored();
        long newEnergyStored = Math.min(getLargeEnergyStored() + energy, getLargeMaxEnergyStored());
        if (!simulate) {
            MultiEnergyNetworkManager.setEnergyStored(node, newEnergyStored);
            onContentsChanged();
        }
        return (int)(newEnergyStored - energyBefore);
    }

    @Override
    public int takeEnergy(int energy) {
        long energyBefore = getLargeEnergyStored();
        MultiEnergyNetworkManager.setEnergyStored(node, getLargeEnergyStored() - energy);
        return (int)(energyBefore - getEnergyStored());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || getMaxEnergyStored() == 0) {
            return 0;
        }

        int energyReceived = (int)Math.min(Math.min(getLargeMaxEnergyStored() - getLargeEnergyStored(), maxReceive), Integer.MAX_VALUE);
        if (!simulate) {
            addEnergy(energyReceived);
        }

        return energyReceived;
    }

    @Override
    public void setEnergyStored(int energy) {
        throw new IllegalStateException("Cannot call setEnergyStored on MultiEnergyNetworkCapability.");
    }
}
