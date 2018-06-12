package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.util.ResourceLocation;

public class ModTransferTypes {
  public static final TransferTypeItems itemType = new TransferTypeItems();
  public static final TransferTypeFluids fluidType = new TransferTypeFluids();
  public static final TransferTypeEnergy energyType = new TransferTypeEnergy();

  public static void registerTransferTypes() {
    TransferType.register(itemType, new ResourceLocation(FiberOptics.MODID, "item"));
    TransferType.register(fluidType, new ResourceLocation(FiberOptics.MODID, "fluid"));
    TransferType.register(energyType, new ResourceLocation(FiberOptics.MODID, "forge_energy"));
  }
}
