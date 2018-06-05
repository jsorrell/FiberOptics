package com.jsorrell.fiberoptics.message.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Arrays;

public class PacketSetSide implements IMessage {
  private BlockPos pos;
  @Nullable
  private EnumFacing side;
  public PacketSetSide() {

  }

  public PacketSetSide(BlockPos pos, @Nullable EnumFacing side) {
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

  public static class Handler implements IMessageHandler<PacketSetSide, PacketOpenConnectionChooser> {
    @Override
    public PacketOpenConnectionChooser onMessage(PacketSetSide message, MessageContext ctx) {
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
