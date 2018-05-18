package com.jsorrell.fiberoptics.transfer_type;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TransferTypeFluids extends TransferType<IFluidHandler> {
  @Override
  public Capability<IFluidHandler> getCapability() {
    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(IFluidHandler input) {
    return input.drain(1, false).amount > 0;
  }

  @Override
  public boolean doTransfer(IFluidHandler input, IFluidHandler output) {
    FluidStack available = input.drain(1000, false);
    int transferred = output.fill(available, true);
    if (transferred > 0) {
      available.amount = transferred;
      input.drain(available, true);
      return true;
    }
    return false;
  }

  @Override
  public String getUnlocalizedName() {
    return "forge_fluids";
  }
}
