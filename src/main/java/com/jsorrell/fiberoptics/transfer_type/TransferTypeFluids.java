package com.jsorrell.fiberoptics.transfer_type;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class TransferTypeFluids extends TransferType<IFluidHandler> {
  @Override
  public Capability<IFluidHandler> getCapability() {
    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(@Nonnull IFluidHandler input) {
    FluidStack drained = input.drain(1, false);
    return drained != null && drained.amount > 0;
  }

  @Override
  public boolean doTransfer(@Nonnull IFluidHandler input, @Nonnull IFluidHandler output) {
    FluidStack available = input.drain(1000, false);
    if (available == null || available.amount == 0)
      return false;
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
