package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.transfer_type.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class OpticalFiberOutput extends OpticalFiberConnection {
  public OpticalFiberOutput(BlockPos pos, EnumFacing connectedSide, TransferType transferType) {
    super(pos, connectedSide, transferType);
  }

  @SuppressWarnings("unused")
  public OpticalFiberOutput(ByteBuf buf) {
    super(buf);
  }

  @Override
  public TransferDirection getTransferDirection() {
    return TransferDirection.OUTPUT;
  }
}