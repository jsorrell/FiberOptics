package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import static com.google.common.base.Ascii.NUL;

public class OpticalFiberConnectionCreationRequest implements IMessage {
  public OpticalFiberConnectionCreationRequest() {}

  private OpticalFiberConnection connection;

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

      WorldServer world = ctx.getServerHandler().player.getServerWorld();

      // Prevent arbitrary chunk generation
      if (!world.isBlockLoaded(message.connection.pos)) {
        return null;
      }

      // Must be run on main thread not network thread
      //TODO use result of this to send response of success or failure?
      world.addScheduledTask(() -> message.connection.initialize(world));
      return null;
    }
  }
}