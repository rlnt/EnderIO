package com.enderio.machines.common.io.energy;

/**
 * Machine energy storage extensions.
 */
// NOTE: Using 'I' prefix here for consistency with Neo EnergyStorage
// TODO: Rename ILargeEnergyStorage, has nothing to do with machines really
public interface ILargeMachineEnergyStorage {

    long getLargeEnergyStored();

    long getLargeMaxEnergyStored();
}
