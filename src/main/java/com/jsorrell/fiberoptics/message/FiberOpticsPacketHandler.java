package com.jsorrell.fiberoptics.message;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class FiberOpticsPacketHandler {
  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(FiberOptics.MODID);
  private static int discriminator = 0;

  public static void registerPacketHandlers() {
    INSTANCE.registerMessage(OpticalFiberConnectionCreationMessage.Handler.class, OpticalFiberConnectionCreationMessage.class, discriminator++, Side.SERVER);
  }
}
