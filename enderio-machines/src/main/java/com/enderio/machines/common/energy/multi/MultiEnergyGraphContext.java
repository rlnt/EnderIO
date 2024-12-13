package com.enderio.machines.common.energy.multi;

import dev.gigaherz.graph3.Mergeable;
import net.minecraft.util.Mth;

public class MultiEnergyGraphContext implements Mergeable<MultiEnergyGraphContext> {

    private int maxEnergyStoredPerNode;
    private long energyStored;
    private boolean isSavedToNodes;

    public MultiEnergyGraphContext(int maxEnergyStoredPerNode) {
        this.maxEnergyStoredPerNode = maxEnergyStoredPerNode;
    }

    public MultiEnergyGraphContext(int maxEnergyStoredPerNode, long energyStored) {
        this.energyStored = energyStored;
        this.maxEnergyStoredPerNode = maxEnergyStoredPerNode;
    }

    public int maxEnergyStoredPerNode() {
        return maxEnergyStoredPerNode;
    }

    public boolean isSavedToNodes() {
        return isSavedToNodes;
    }

    public void setIsSavedToNodes(boolean isSavedToNodes) {
        this.isSavedToNodes = isSavedToNodes;
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(long energyStored) {
        this.energyStored = energyStored;
        isSavedToNodes = false;
    }

    @Override
    public MultiEnergyGraphContext mergeWith(MultiEnergyGraphContext other) {
        return new MultiEnergyGraphContext(maxEnergyStoredPerNode, energyStored + other.energyStored);
    }

    @Override
    public MultiEnergyGraphContext splitFor(int selfNodeCount, int totalNodeCount) {
        if (selfNodeCount == 0 || totalNodeCount == 0) {
            return new MultiEnergyGraphContext(0);
        }

        float ratio = selfNodeCount / (float) totalNodeCount;
        float carriedEnergy = ratio * energyStored;
        return new MultiEnergyGraphContext(maxEnergyStoredPerNode, Math.round(carriedEnergy));
    }

    public int getAmountToSave(int nodeCount, boolean withRemainder) {
        long tempEnergyStored = Mth.clamp(energyStored, 0, (long)maxEnergyStoredPerNode * nodeCount);

        int localEnergy = (int)(tempEnergyStored / nodeCount);
        if (withRemainder) {
            localEnergy += (int)(tempEnergyStored % nodeCount);
        }

        return localEnergy;
    }
}
