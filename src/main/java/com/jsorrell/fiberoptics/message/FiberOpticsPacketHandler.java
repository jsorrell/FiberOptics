package com.jsorrell.fiberoptics.message;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.message.optical_fiber.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class FiberOpticsPacketHandler {
  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(FiberOptics.MODID);
  private static int discriminator = 0;

  public static void registerPacketHandlers() {
    /* Edit Connections */
    INSTANCE.registerMessage(PacketCreateConnection.Handler.class, PacketCreateConnection.class, discriminator++, Side.SERVER);
    INSTANCE.registerMessage(PacketRemoveConnection.Handler.class, PacketRemoveConnection.class, discriminator++, Side.SERVER);

    /* Open GUIs */
    // Side
    INSTANCE.registerMessage(PacketOpenSideChooser.Handler.class, PacketOpenSideChooser.class, discriminator++, Side.CLIENT);
    // Connection
    INSTANCE.registerMessage(PacketOpenConnectionChooser.Request.Handler.class, PacketOpenConnectionChooser.Request.class, discriminator++, Side.SERVER);
    INSTANCE.registerMessage(PacketOpenConnectionChooser.Handler.class, PacketOpenConnectionChooser.class, discriminator++, Side.CLIENT);
    // Channel
    INSTANCE.registerMessage(PacketOpenChannelChooser.Request.Handler.class, PacketOpenChannelChooser.Request.class, discriminator++, Side.SERVER);
    INSTANCE.registerMessage(PacketOpenChannelChooser.Handler.class, PacketOpenChannelChooser.class, discriminator++, Side.CLIENT);
    // Type
    INSTANCE.registerMessage(PacketOpenTypeChooser.Request.Handler.class, PacketOpenTypeChooser.Request.class, discriminator++, Side.SERVER);
    INSTANCE.registerMessage(PacketOpenTypeChooser.Handler.class, PacketOpenTypeChooser.class, discriminator++, Side.CLIENT);

    /* Sync */
    INSTANCE.registerMessage(PacketClientSync.Handler.class, PacketClientSync.class, discriminator++, Side.CLIENT);
    INSTANCE.registerMessage(PacketClientSync.Request.Handler.class, PacketClientSync.Request.class, discriminator++, Side.SERVER);
  }
}
