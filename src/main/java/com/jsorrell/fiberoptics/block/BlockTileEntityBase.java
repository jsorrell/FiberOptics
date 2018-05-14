package com.jsorrell.fiberoptics.block;

import jline.internal.Nullable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockTileEntityBase<TE extends TileEntity> extends BlockBase implements ITileEntityProvider {
  public BlockTileEntityBase(Material material, String name) {
    super(material, name);
  }

  public abstract Class<TE> getTileEntityClass();

  public TE getTileEntity(IBlockAccess world, BlockPos pos) {
    return (TE)world.getTileEntity(pos);
  }

  @Override
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }

  @Nullable
  @Override
  public abstract TileEntity createNewTileEntity(World worldIn, int meta);
}
