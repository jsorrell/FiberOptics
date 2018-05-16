package com.jsorrell.fiberoptics.message;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import static com.google.common.base.Ascii.NUL;

public class OpticalFiberConnectionCreationMessage implements IMessage {
  public OpticalFiberConnectionCreationMessage() {}

  OpticalFiberConnection connection;

  public OpticalFiberConnectionCreationMessage(OpticalFiberConnection connection) {
    this.connection = connection;
  }


  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeBytes(connection.getClass().getName().getBytes(StandardCharsets.US_ASCII));
    buf.writeByte(NUL);
    connection.toBytes(buf);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    String connectionClassName = "";
    Constructor connectionClassConstructor;
    try {
      int connectionClassNameLength = buf.bytesBefore(NUL);
      byte[] connectionClassNameBytes = new byte[connectionClassNameLength];
      buf.readBytes(connectionClassNameBytes);
      // Remove the NUL
      buf.readByte();
      connectionClassName = new String(connectionClassNameBytes, StandardCharsets.US_ASCII);
      // Class that extends OpticalFiberConnection
      Class<?> connectionClass = Class.forName(connectionClassName);
      if (!OpticalFiberConnection.class.isAssignableFrom(connectionClass)) {
        FiberOptics.LOGGER.log(Level.WARNING, "Connection " + connectionClassName + " must be a subclass of " + OpticalFiberConnection.class);
        return;
      }
      connectionClassConstructor = connectionClass.getConstructor(ByteBuf.class);
      this.connection = (OpticalFiberConnection)connectionClassConstructor.newInstance(buf);
    } catch (NoSuchMethodException e) {
      FiberOptics.LOGGER.log(Level.WARNING, "Connection " + connectionClassName + " requires a constructor that takes a ByteBuf");
    } catch (Exception e) {
      FiberOptics.LOGGER.log(Level.WARNING, "Invalid packet received when creating new connection: " + e);
    }
  }

  public static class Handler implements IMessageHandler<OpticalFiberConnectionCreationMessage, IMessage> {
    @Override
    public IMessage onMessage(OpticalFiberConnectionCreationMessage message, MessageContext ctx) {
      // If the message was garbled
      if (message.connection == null) {
        return null;
      }

      EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

      // Prevent arbitrary chunk generation
      if (!serverPlayer.world.isBlockLoaded(message.connection.getPos())) {
        return null;
      }

      TileOpticalFiberController controllerTile = message.connection.getController(serverPlayer.world);
      // Must be run on main thread not network thread
      serverPlayer.getServerWorld().addScheduledTask(() -> controllerTile.addConnection(message.connection));
      return null;
    }
  }
}