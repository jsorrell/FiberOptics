package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketClientSync;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileOpticalFiberClient extends TileEntity {
  private IBlockState cover = null;

  @Override
  public void onLoad() {
    FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketClientSync.Request(this.pos));
  }
}
