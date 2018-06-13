package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiScreenTransferTypeChooser;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PacketOpenTransferTypeChooser implements IMessage {
  private BlockPos pos;
  private EnumFacing side;
  private String channel;
  private List<TransferType> transferTypes;

  @SuppressWarnings("unused")
  public PacketOpenTransferTypeChooser() { }

  public PacketOpenTransferTypeChooser(BlockPos pos, EnumFacing side, String channel, Collection<TransferType> transferTypes) {
    this.pos = pos;
    this.side = side;
    this.channel = channel;
    this.transferTypes = new ArrayList<>(transferTypes);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    SerializeUtils.writeBlockPos(buf, this.pos);
    SerializeUtils.writeEnumFacing(buf, this.side);
    SerializeUtils.writeUTF8String(buf, this.channel);
    SerializeUtils.writeList(buf, this.transferTypes, SerializeUtils::writeTransferType);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = SerializeUtils.readBlockPos(buf);
    this.side = SerializeUtils.readEnumFacing(buf);
    this.channel = SerializeUtils.readUTF8String(buf);
    this.transferTypes = SerializeUtils.readList(buf, SerializeUtils::readTransferType).stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  /* Client Side */
  public static class Handler implements IMessageHandler<PacketOpenTransferTypeChooser, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenTransferTypeChooser message, MessageContext ctx) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiScreenTransferTypeChooser(message.pos, message.side, message.channel, message.transferTypes));
      return null;
    }
  }

  /* Request */
  public static class Request implements IMessage {
    private BlockPos pos;
    private EnumFacing side;
    private String channel;

    @SuppressWarnings("unused")
    public Request() { }

    public Request(BlockPos pos, EnumFacing side, String channel) {
      this.pos = pos;
      this.side = side;
      this.channel = channel;
    }

    @Override
    public void toBytes(ByteBuf buf) {
      SerializeUtils.writeBlockPos(buf, this.pos);
      SerializeUtils.writeEnumFacing(buf, this.side);
      SerializeUtils.writeUTF8String(buf, this.channel);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
      this.pos = SerializeUtils.readBlockPos(buf);
      this.side = SerializeUtils.readEnumFacing(buf);
      this.channel = SerializeUtils.readUTF8String(buf);
    }

    /* Server Side */
    public static class Handler implements IMessageHandler<PacketOpenTransferTypeChooser.Request, PacketOpenTransferTypeChooser> {
      @Override
      public PacketOpenTransferTypeChooser onMessage(Request message, MessageContext ctx) {
        WorldServer world = ctx.getServerHandler().player.getServerWorld();

        if (!world.isBlockLoaded(message.pos) || !BlockOpticalFiber.isFiberInPos(world, message.pos)) {
          return null;
        }
        TileOpticalFiberBase tile = Util.getTileChecked(world, message.pos, TileOpticalFiberBase.class);

        return new PacketOpenTransferTypeChooser(message.pos, message.side, message.channel, tile.getAvailableTypes());
      }
    }
  }
}
