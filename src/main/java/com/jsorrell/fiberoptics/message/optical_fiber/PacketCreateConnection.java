package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.utils.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class PacketCreateConnection implements IMessage {
  @SuppressWarnings("unused")
  public PacketCreateConnection() {}

  private OpticalFiberConnection connection;
  private OpticalFiberConnection connectionToReplace;

  public PacketCreateConnection(OpticalFiberConnection connection) {
    this(connection, null);
  }

  public PacketCreateConnection(OpticalFiberConnection connection, @Nullable OpticalFiberConnection connectionToReplace) {
    this.connection = connection;
    this.connectionToReplace = connectionToReplace;
  }


  @Override
  public void toBytes(ByteBuf buf) {
    connection.toKeyedBytes(buf);

    buf.writeBoolean(connectionToReplace != null);
    if (connectionToReplace != null) {
      connectionToReplace.toKeyedBytes(buf);
    }
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.connection = OpticalFiberConnection.fromKeyedBytes(buf);
    if (buf.readBoolean()) {
      this.connectionToReplace = OpticalFiberConnection.fromKeyedBytes(buf);
    }
  }

  public static class Handler implements IMessageHandler<PacketCreateConnection, IMessage> {
    @Override
    public IMessage onMessage(PacketCreateConnection message, MessageContext ctx) {
      WorldServer world = ctx.getServerHandler().player.getServerWorld();

      // Prevent arbitrary chunk generation
      if (!world.isBlockLoaded(message.connection.pos)) {
        return null;
      }

      // Must be run on main thread not network thread
      //TODO use result of this to send response of success or failure?
      world.addScheduledTask(() -> {
        if (message.connectionToReplace == null) {
          Util.getTileChecked(world, message.connection.pos, TileOpticalFiberBase.class).addConnection(message.connection);
        } else {
          Util.getTileChecked(world, message.connection.pos, TileOpticalFiberBase.class).replaceConnection(message.connection, message.connectionToReplace);
        }
      });
      return null;
    }
  }
}