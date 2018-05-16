package com.jsorrell.fiberoptics.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class OpticalFiberItemOutput extends OpticalFiberConnection implements OpticalFiberOutput {
  public OpticalFiberItemOutput(BlockPos pos, EnumFacing connectedSide) {
    super(pos, connectedSide);
  }

  public OpticalFiberItemOutput(ByteBuf buf) {
    super(buf);
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.ITEMS;
  }

  @Nullable
  @Override
  public Object getServing() {
    return null;
  }
}
