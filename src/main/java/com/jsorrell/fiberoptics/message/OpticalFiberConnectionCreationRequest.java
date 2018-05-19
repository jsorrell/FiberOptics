package com.jsorrell.fiberoptics.message;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
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

public class OpticalFiberConnectionCreationRequest implements IMessage {
  public OpticalFiberConnectionCreationRequest() {}

  OpticalFiberConnection connection;

  public OpticalFiberConnectionCreationRequest(OpticalFiberConnection connection) {
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

  public static class Handler implements IMessageHandler<OpticalFiberConnectionCreationRequest, IMessage> {
    @Override
    public IMessage onMessage(OpticalFiberConnectionCreationRequest message, MessageContext ctx) {
      // If the message was garbled
      if (message.connection == null) {
        return null;
      }

      EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

      // Prevent arbitrary chunk generation
      if (!serverPlayer.world.isBlockLoaded(message.connection.getPos())) {
        return null;
      }

      System.out.println(message.connection.getPos());

      TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(serverPlayer.world, message.connection.getPos());
      TileOpticalFiberController controllerTile = TileOpticalFiberController.getTileEntity(serverPlayer.world, tile.getControllerPos());
      // Must be run on main thread not network thread
      serverPlayer.getServerWorld().addScheduledTask(() -> controllerTile.addConnection(message.connection));
      return null;
    }
  }
}