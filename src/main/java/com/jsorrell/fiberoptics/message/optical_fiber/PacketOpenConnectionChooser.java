package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiConnectionChooser;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketOpenConnectionChooser implements IMessage {
  private BlockPos pos;
  @Nullable
  private EnumFacing side;
  private List<OpticalFiberConnection> connections;

  public PacketOpenConnectionChooser() {}

  public PacketOpenConnectionChooser(BlockPos pos, EnumFacing direction, Collection<OpticalFiberConnection> connections) {
    this.pos = pos;
    this.side = direction;
    this.connections = new ArrayList<>(connections);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(pos.toLong());
    if (side == null) {
      buf.writeInt(-1);
    } else {
      buf.writeInt(side.getIndex());
    }
    buf.writeInt(this.connections.size());
    for (OpticalFiberConnection connection : connections) {
      connection.toKeyedBytes(buf);
    }
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    int sideInt = buf.readInt();
    if (sideInt == -1) {
      this.side = null;
    } else {
      this.side = EnumFacing.getFront(sideInt);
    }
    int numConnections = buf.readInt();
    this.connections = new ArrayList<>(numConnections);
    for (int i = 0; i < numConnections; ++i) {
      this.connections.add(OpticalFiberConnection.fromKeyedBytes(buf));
    }
  }

  public static class Handler implements IMessageHandler<PacketOpenConnectionChooser, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenConnectionChooser message, MessageContext ctx) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiConnectionChooser(message.pos, message.side, message.connections));
      return null;
    }
  }
}
