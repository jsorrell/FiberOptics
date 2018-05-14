package com.jsorrell.fiberoptics.block;

import net.minecraft.tileentity.TileEntity;

public abstract class TileOpticalFiberBase extends TileEntity {
  public abstract TileOpticalFiberController getControllerTile();
}
