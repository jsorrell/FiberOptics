package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.optical_fiber.FiberSideType;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.message.MessagePos;
import com.jsorrell.fiberoptics.utils.Util;
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
import java.util.Arrays;
import java.util.List;

public class PacketClientSync extends MessagePos {
  private final List<FiberSideType> sides = Arrays.asList(new FiberSideType[6]);

  public PacketClientSync() {}

  public PacketClientSync(BlockPos pos) {
    super(pos);
  }

  private int encodeSides() {
    assert Util.iPow(FiberSideType.values().length, 6) <= Integer.MAX_VALUE;
    int out = 0;
    for (int i = 0; i < 6; ++i) {
      out += sides.get(i).getIndex() * Util.iPow(FiberSideType.values().length, i);
    }
    return out;
  }

  private void decodeSides(int in) {
    for (int i = 5; i >= 0; --i) {
      int pow = (int) Util.iPow(FiberSideType.values().length, i);
      int val = in / pow;
      sides.set(i, FiberSideType.fromIndex(val));
      in -= val * pow;
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeInt(encodeSides());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    decodeSides(buf.readInt());
  }

  public void setSide(@Nonnull EnumFacing side, FiberSideType type) {
    this.sides.set(side.getIndex(), type);
  }


  // Client Side
  public static class Handler implements IMessageHandler<PacketClientSync, IMessage> {
    @Override
    public IMessage onMessage(PacketClientSync message, MessageContext ctx) {
      World world = Minecraft.getMinecraft().world;
      if (world.isBlockLoaded(message.pos) && BlockOpticalFiber.isFiberInPos(world, message.pos)) {
        IBlockState state = world.getBlockState(message.pos);
        for (int i = 0; i < 6; ++i) {
          state = state.withProperty(BlockOpticalFiber.getPropertyFromSide(EnumFacing.getFront(i)), message.sides.get(i));
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

        PacketClientSync response = new PacketClientSync(message.pos);

        for (EnumFacing side : EnumFacing.VALUES) {
          response.setSide(side, world.getBlockState(message.pos).getValue(BlockOpticalFiber.getPropertyFromSide(side)));
        }

        return response;
      }
    }
  }
}
