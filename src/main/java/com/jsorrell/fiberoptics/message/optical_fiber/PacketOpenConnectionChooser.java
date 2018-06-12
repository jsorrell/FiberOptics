package com.jsorrell.fiberoptics.message.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiScreenConnectionChooser;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PacketOpenConnectionChooser implements IMessage {
  private BlockPos pos;
  private List<EnumFacing> sidesToDisplay;
  private List<OpticalFiberConnection> connections;

  public PacketOpenConnectionChooser() {}

  public PacketOpenConnectionChooser(BlockPos pos, Collection<EnumFacing> sidesToDisplay, Collection<OpticalFiberConnection> connections) {
    this.pos = pos;
    this.sidesToDisplay = new ArrayList<>(sidesToDisplay);
    this.connections = new ArrayList<>(connections);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(pos.toLong());

    buf.writeInt(this.sidesToDisplay.size());
    for (EnumFacing side : this.sidesToDisplay) {
      buf.writeByte(side.getIndex());
    }

    buf.writeInt(this.connections.size());
    for (OpticalFiberConnection connection : this.connections) {
      connection.toBytes(buf);
    }
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());

    int numSides = buf.readInt();
    this.sidesToDisplay = new ArrayList<>(numSides);
    for (int i = 0; i < numSides; ++i) {
      this.sidesToDisplay.add(EnumFacing.getFront(buf.readByte()));
    }

    int numConnections = buf.readInt();
    this.connections = new ArrayList<>(numConnections);
    for (int i = 0; i < numConnections; ++i) {
      this.connections.add(OpticalFiberConnection.fromBytes(buf));
    }
  }

  // Client Side
  public static class Handler implements IMessageHandler<PacketOpenConnectionChooser, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenConnectionChooser message, MessageContext ctx) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiScreenConnectionChooser(message.pos, message.sidesToDisplay, message.connections));
      return null;
    }
  }


  public static class Request implements IMessage {
    private BlockPos pos;
    @Nullable
    private EnumFacing side;
    public Request() { }

    public Request(BlockPos pos, @Nullable EnumFacing side) {
      this.pos = pos;
      this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buf) {
      buf.writeLong(this.pos.toLong());
      if (this.side == null) {
        buf.writeInt(-1);
      } else {
        buf.writeInt(this.side.getIndex());
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
    }

    public static class Handler implements IMessageHandler<Request, PacketOpenConnectionChooser> {
      @Override
      public PacketOpenConnectionChooser onMessage(Request message, MessageContext ctx) {
        World world = ctx.getServerHandler().player.world;
        if (!world.isBlockLoaded(message.pos) || !BlockOpticalFiber.isFiberInPos(world, message.pos)) {
          return null;
        }
        TileOpticalFiberBase tile = Util.getTileChecked(world, message.pos, TileOpticalFiberBase.class);
        // TODO send sides with possible connections only
        return new PacketOpenConnectionChooser(message.pos, message.side == null ? Arrays.asList(EnumFacing.VALUES) : ImmutableList.of(message.side), tile.getConnections(message.side));
      }
    }
  }
}
