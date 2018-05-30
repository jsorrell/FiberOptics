package com.jsorrell.fiberoptics.message;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.message.optical_fiber.OpticalFiberConnectionCreationRequest;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenSideChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketSetSide;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class FiberOpticsPacketHandler {
  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(FiberOptics.MODID);
  private static int discriminator = 0;

  public static void registerPacketHandlers() {
    INSTANCE.registerMessage(OpticalFiberConnectionCreationRequest.Handler.class, OpticalFiberConnectionCreationRequest.class, discriminator++, Side.SERVER);
    INSTANCE.registerMessage(PacketOpenSideChooser.Handler.class, PacketOpenSideChooser.class, discriminator++, Side.CLIENT);
    INSTANCE.registerMessage(PacketOpenConnectionChooser.Handler.class, PacketOpenConnectionChooser.class, discriminator++, Side.CLIENT);
    INSTANCE.registerMessage(PacketSetSide.Handler.class, PacketSetSide.class, discriminator++, Side.SERVER);
  }
}
