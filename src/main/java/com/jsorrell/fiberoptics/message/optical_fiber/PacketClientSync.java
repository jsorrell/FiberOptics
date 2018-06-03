package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.FiberSideType;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberClient;
import com.jsorrell.fiberoptics.message.MessagePos;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.BitSet;

public class PacketClientSync extends MessagePos {
  private final BitSet connectedSides = new BitSet(6);

  public PacketClientSync() {
  }

  public PacketClientSync(BlockPos pos) {
    super(pos);
  }

  private byte bitsToByte() {
    byte out = 0;
    for (int i = 0; i < 6; ++i) {
      if (this.connectedSides.get(i)) {
        out |= 0x1 << i;
      }
    }
    return out;
  }

  private void byteToBits(byte in) {
    for (int i = 0; i < 8; ++i) {
      if ((in & (0x1 << i)) != 0) {
        this.connectedSides.set(i);
      }
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf );
    buf.writeByte(bitsToByte());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    byteToBits(buf.readByte());
  }

  public void setConnectedSide(@Nonnull EnumFacing side) {
    this.connectedSides.set(side.getIndex());
  }


  // Client Side
  public static class Handler implements IMessageHandler<PacketClientSync, IMessage> {
    @Override
    public IMessage onMessage(PacketClientSync message, MessageContext ctx) {
      World world = Minecraft.getMinecraft().world;
      if (world.isBlockLoaded(message.pos) && BlockOpticalFiber.isFiberInPos(world, message.pos)) {
        IBlockState state = world.getBlockState(message.pos);
        for (EnumFacing side : EnumFacing.VALUES) {
          if (message.connectedSides.get(side.getIndex())) {
            state = state.withProperty(BlockOpticalFiber.getPropertyFromSide(side), FiberSideType.CONNECTION);
          }
        }
        world.setBlockState(message.pos, state);
      }

      return null;
    }
  }

  public static class Request extends MessagePos {
    public Request() {}

    public Request(BlockPos pos) {
      super(pos);
    }

    // Server Side
    public static class Handler implements IMessageHandler<Request, PacketClientSync> {
      @Override
      public PacketClientSync onMessage(Request message, MessageContext ctx) {
        WorldServer world = ctx.getServerHandler().player.getServerWorld();

        // Prevent arbitrary chunk generation
        if (!world.isBlockLoaded(message.pos)) {
          return null;
        }

        TileEntity testTile = world.getTileEntity(message.pos);
        if (!(testTile instanceof TileOpticalFiberBase)) {
          return null;
        }

        TileOpticalFiberBase tile = (TileOpticalFiberBase) testTile;

        PacketClientSync response = new PacketClientSync(message.pos);

        for (EnumFacing side : EnumFacing.VALUES) {
          if (tile.hasConnectionOnSide(side)) response.setConnectedSide(side);
        }

        return response;
      }
    }
  }
}
