package com.jsorrell.fiberoptics.connection;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class OpticalFiberItemInput extends OpticalFiberConnection implements OpticalFiberInput {
  public OpticalFiberItemInput(BlockPos pos, EnumFacing connectedSide) {
    super(pos, connectedSide);
  }

  public OpticalFiberItemInput(ByteBuf buf) {
    super(buf);
  }

  @Override
  public ConnectionType getConnectionType() {
    return ConnectionType.ITEMS;
  }

  @Override
  public int canAccept(@Nonnull Object o) {
    if (!(o instanceof ItemStack)) {
      return 0;
    }
    return 0;
  }
}
