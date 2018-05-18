package com.jsorrell.fiberoptics.transfer_type;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TransferTypeEnergy extends TransferType<IEnergyStorage> {
  @Override
  public Capability<IEnergyStorage> getCapability() {
    return CapabilityEnergy.ENERGY;
  }

  @Override
  public boolean isSource(TileEntity tile, EnumFacing side) {
    IEnergyStorage capabilityHandler;
    if ((capabilityHandler = tile.getCapability(this.getCapability(), side)) != null) {
      return capabilityHandler.canExtract();
    }
    return false;
  }

  @Override
  public boolean isSink(TileEntity tile, EnumFacing side) {
    IEnergyStorage capabilityHandler;
    if ((capabilityHandler = tile.getCapability(this.getCapability(), side)) != null) {
      return capabilityHandler.canReceive();
    }
    return false;
  }

  @Override
  public boolean isOffering(IEnergyStorage input) {
    return input.extractEnergy(1, true) > 0;
  }

  @Override
  public boolean doTransfer(IEnergyStorage input, IEnergyStorage output) {
    int available = input.extractEnergy(1000, true);
    int transferred = output.receiveEnergy(available, false);
    if (transferred > 0) {
      input.extractEnergy(transferred, false);
      return true;
    }
    return false;
  }

  @Override
  public String getUnlocalizedName() {
    return "forge_energy";
  }
}