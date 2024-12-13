package com.enderio.machines.common.energy.multi;

import dev.gigaherz.graph3.GraphObject;

public interface MultiEnergyNode extends GraphObject<MultiEnergyGraphContext> {
    int getLocalEnergyStored();
    void setLocalEnergyStored(int energyStored);
}
