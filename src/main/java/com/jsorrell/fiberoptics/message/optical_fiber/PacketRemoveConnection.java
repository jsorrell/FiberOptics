package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveConnection implements IMessage {
  private OpticalFiberConnection connection;
  public PacketRemoveConnection() { }

  public PacketRemoveConnection(OpticalFiberConnection connection) {
    this.connection = connection;
  }

  @Override
  public void toBytes(ByteBuf buf) {
    this.connection.toBytes(buf);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.connection = OpticalFiberConnection.fromBytes(buf);
  }

  // Sever side
  public static class Handler implements IMessageHandler<PacketRemoveConnection, IMessage> {
    @Override
    public IMessage onMessage(PacketRemoveConnection message, MessageContext ctx) {

      WorldServer world = ctx.getServerHandler().player.getServerWorld();

      // Prevent arbitrary chunk generation
      if (!world.isBlockLoaded(message.connection.pos)) {
        return null;
      }

      // Must be run on main thread not network thread
      //TODO use result of this to send response of success or failure?
      world.addScheduledTask(() -> Util.getTileChecked(world, message.connection.pos, TileOpticalFiberBase.class).removeConnection(message.connection));
      return null;
    }
  }
}
