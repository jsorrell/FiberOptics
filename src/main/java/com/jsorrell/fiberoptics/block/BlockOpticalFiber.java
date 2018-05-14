package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class BlockOpticalFiber extends BlockOpticalFiberBase<TileOpticalFiber> {

  public BlockOpticalFiber() {
    super("optical_fiber");
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileOpticalFiber();
  }

  @Override
  public Class<TileOpticalFiber> getTileEntityClass() {
    return TileOpticalFiber.class;
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    if (!worldIn.isRemote && isLoneFiber(worldIn, pos)) {
      worldIn.setBlockState(pos, ModBlocks.opticalFiberController.getDefaultState());
    }
  }
}
