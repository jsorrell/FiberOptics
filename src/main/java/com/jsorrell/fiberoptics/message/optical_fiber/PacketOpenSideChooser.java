package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiScreenSideChooser;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketOpenSideChooser implements IMessage {
  private BlockPos pos;
  private List<OpticalFiberConnection> connections;

  public PacketOpenSideChooser() { }

  public PacketOpenSideChooser(BlockPos pos) {
    this.pos = pos;
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(pos.toLong());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
  }

  public static class Handler implements IMessageHandler<PacketOpenSideChooser, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenSideChooser message, MessageContext ctx) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiScreenSideChooser(message.pos));
      return null;
    }
  }
}

