package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

class OpticalFiberItemOutput extends OpticalFiberConnection implements OpticalFiberOutput {
  public OpticalFiberItemOutput(BlockPos pos, TileOpticalFiberController controller, TileEntity connectedTile, EnumFacing connectedSide) {
    super(pos, controller, connectedTile, connectedSide);
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
