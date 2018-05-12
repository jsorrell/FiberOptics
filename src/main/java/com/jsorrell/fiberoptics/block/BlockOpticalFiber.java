package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class BlockOpticalFiber extends BlockBase {
  public BlockOpticalFiber() {
    super(Material.ROCK, "optical_fiber");

    setHardness(3f);
    setResistance(5000f);
  }

  @Override
  public BlockOpticalFiber setCreativeTab(CreativeTabs tab) {
    super.setCreativeTab(tab);
    return this;
  }

  @Override
  @Deprecated
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  @Deprecated
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
  {
    return new AxisAlignedBB(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);
  }
}
