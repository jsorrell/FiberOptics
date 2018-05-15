package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

class OpticalFiberItemInput extends OpticalFiberConnection implements OpticalFiberInput {
  public OpticalFiberItemInput(BlockPos pos, TileOpticalFiberController controller, TileEntity connectedTile, EnumFacing connectedSide) {
    super(pos, controller, connectedTile, connectedSide);
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
