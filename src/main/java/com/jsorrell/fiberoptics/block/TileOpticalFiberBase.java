package com.jsorrell.fiberoptics.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public abstract class TileOpticalFiberBase extends TileEntity {
  public abstract BlockPos getControllerPos();
  public abstract boolean isController();
}
