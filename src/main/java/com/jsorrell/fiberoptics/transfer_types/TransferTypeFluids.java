package com.jsorrell.fiberoptics.transfer_types;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.reflect.Type;

public class TransferTypeFluids extends TransferType<IFluidHandler> {

  @Override
  public Type getTransferObjectType() {
    return FluidStack.class;
  }

  @Override
  public String toString() {
    return "forge_fluids";
  }

  @Override
  public Capability<IFluidHandler> getCapability() {
    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(IFluidHandler capabilityHandler) {
    return false;
  }
}
