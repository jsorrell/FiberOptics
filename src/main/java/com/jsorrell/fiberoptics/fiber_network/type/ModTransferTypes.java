package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.util.ResourceLocation;

public class ModTransferTypes {
  public static void registerTransferTypes() {
    TransferType.registerType(new TransferTypeItems(new ResourceLocation(FiberOptics.MODID, "items")));
    TransferType.registerType(new TransferTypeFluids(new ResourceLocation(FiberOptics.MODID, "fluids")));
    TransferType.registerType(new TransferTypeEnergy(new ResourceLocation(FiberOptics.MODID, "forge_energy")));
  }
}