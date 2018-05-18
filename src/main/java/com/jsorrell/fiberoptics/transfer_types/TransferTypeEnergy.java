package com.jsorrell.fiberoptics.transfer_types;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.reflect.Type;

public class TransferTypeEnergy extends TransferType<IEnergyStorage> {
  @Override
  public Type getTransferObjectType() {
    return int.class;
  }

  @Override
  public String toString() {
    return "forge_energy";
  }

  @Override
  public Capability<IEnergyStorage> getCapability() {
    return CapabilityEnergy.ENERGY;
  }

  @Override
  public boolean isOffering(IEnergyStorage input) {
    return false;
  }

  @Override
  public boolean doTransfer(IEnergyStorage input, IEnergyStorage output) {
    return false;
  }
}