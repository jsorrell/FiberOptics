package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class BlockTileEntityBase<TE extends TileEntity> extends BlockBase {
  public BlockTileEntityBase(Material material, String name) {
    super(material, name);
  }

  public TE getTileEntity(IBlockAccess world, BlockPos pos) {
    return (TE)world.getTileEntity(pos);
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }
}
