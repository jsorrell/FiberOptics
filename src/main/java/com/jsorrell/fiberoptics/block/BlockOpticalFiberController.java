package com.jsorrell.fiberoptics.block;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockOpticalFiberController extends BlockOpticalFiberBase<TileOpticalFiberController> {
  public BlockOpticalFiberController() {
    super("optical_fiber_controller");
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileOpticalFiberController();
  }

  @Override
  public Class<TileOpticalFiberController> getTileEntityClass() {
    return TileOpticalFiberController.class;
  }
}
