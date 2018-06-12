package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiScreenChannelChooser;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketOpenChannelChooser implements IMessage {
  private BlockPos pos;
  private EnumFacing side;
  private List<String> existingChannels;

  @SuppressWarnings("unused")
  public PacketOpenChannelChooser() {}

  public PacketOpenChannelChooser(BlockPos pos, EnumFacing side, List<String> existingChannels) {
    this.pos = pos;
    this.side = side;
    this.existingChannels = existingChannels;
  }

  @Override
  public void toBytes(ByteBuf buf) {
    SerializeUtils.writeBlockPos(buf, this.pos);
    SerializeUtils.writeEnumFacing(buf, this.side);
    SerializeUtils.writeList(buf, existingChannels, SerializeUtils::writeUTF8String);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = SerializeUtils.readBlockPos(buf);
    this.side = SerializeUtils.readEnumFacing(buf);
    this.existingChannels = SerializeUtils.readList(buf, SerializeUtils::readUTF8String);
  }

  // Client Side
  public static class Handler  implements IMessageHandler<PacketOpenChannelChooser, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenChannelChooser message, MessageContext ctx) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiScreenChannelChooser(message.pos, message.side, message.existingChannels));
      return null;
    }
  }

  // Request
  public static class Request implements IMessage {
    private BlockPos pos;
    private EnumFacing side;

    @SuppressWarnings("unused")
    public Request () {}

    public Request(BlockPos pos, EnumFacing side) {
      this.pos = pos;
      this.side = side;
    }

    @Override
    public void toBytes(ByteBuf buf) {
      SerializeUtils.writeBlockPos(buf, this.pos);
      SerializeUtils.writeEnumFacing(buf, this.side);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
      this.pos = SerializeUtils.readBlockPos(buf);
      this.side = SerializeUtils.readEnumFacing(buf);
    }

    // Server Side
    public static class Handler implements IMessageHandler<Request, PacketOpenChannelChooser> {
      @Override
      public PacketOpenChannelChooser onMessage(Request message, MessageContext ctx) {

        WorldServer world = ctx.getServerHandler().player.getServerWorld();

        // Prevent arbitrary chunk generation
        if (!world.isBlockLoaded(message.pos) || !BlockOpticalFiber.isFiberInPos(world, message.pos)) {
          return null;
        }

        TileOpticalFiberBase tile = Util.getTileChecked(world, message.pos, TileOpticalFiberBase.class);

        return new PacketOpenChannelChooser(message.pos, message.side, tile.getChannels());
      }
    }
  }
}
