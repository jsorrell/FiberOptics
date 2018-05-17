package com.jsorrell.fiberoptics.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class OpticalFiberInput extends OpticalFiberConnection {
  public OpticalFiberInput(BlockPos pos, EnumFacing connectedSide, ConnectionType connectionType) {
    super(pos, connectedSide, connectionType);
  }

  public OpticalFiberInput(ByteBuf buf) {
    super(buf);
  }

  @Override
  public ConnectionDirection getConnectionDirection() {
    return ConnectionDirection.INPUT;
  }
}
