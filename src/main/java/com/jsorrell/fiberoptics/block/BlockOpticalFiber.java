package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockOpticalFiber extends BlockBase {

  protected static final PropertyBool upConnected = PropertyBool.create("up");
  protected static final PropertyBool downConnected = PropertyBool.create("down");
  protected static final PropertyBool northConnected = PropertyBool.create("north");
  protected static final PropertyBool southConnected = PropertyBool.create("south");
  protected static final PropertyBool eastConnected = PropertyBool.create("east");
  protected static final PropertyBool westConnected = PropertyBool.create("west");


  public BlockOpticalFiber() {
    super(Material.ROCK, "optical_fiber");

    setHardness(3f);
    setResistance(5000f);
    setLightOpacity(0);

    IBlockState defaultState = this.blockState.getBaseState()
            .withProperty(upConnected, false)
            .withProperty(downConnected, false)
            .withProperty(northConnected, false)
            .withProperty(southConnected, false)
            .withProperty(eastConnected, false)
            .withProperty(westConnected, false);
    setDefaultState(defaultState);
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
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return new AxisAlignedBB(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, upConnected, downConnected, northConnected, southConnected, eastConnected, westConnected);
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    worldIn.getBlockState(pos).getBlock();
    return state.withProperty(upConnected, canConnectTo(worldIn, pos, EnumFacing.UP))
            .withProperty(downConnected, canConnectTo(worldIn, pos, EnumFacing.DOWN))
            .withProperty(northConnected, canConnectTo(worldIn, pos, EnumFacing.NORTH))
            .withProperty(southConnected, canConnectTo(worldIn, pos, EnumFacing.SOUTH))
            .withProperty(eastConnected, canConnectTo(worldIn, pos, EnumFacing.EAST))
            .withProperty(westConnected, canConnectTo(worldIn, pos, EnumFacing.WEST));
  }

  private Boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing direction) {
    BlockPos testPos = pos.offset(direction);
    // Connect to other cables
    if (worldIn.getBlockState(testPos).getBlock() instanceof BlockOpticalFiber) {
      return true;
    }

    // Connect to tile entities
    //TODO: fluid, energy
    TileEntity testTile = worldIn.getTileEntity(testPos);
    if (testTile == null) {
      return false;
    }
    return testTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return 0;
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState();
  }

//  public IBlockState getActualState(IBlockState p_getActualState_1_, IBlockAccess p_getActualState_2_, BlockPos p_getActualState_3_) {
//    return p_getActualState_1_.withProperty(NORTH, this.canFenceConnectTo(p_getActualState_2_, p_getActualState_3_, EnumFacing.NORTH)).withProperty(EAST, this.canFenceConnectTo(p_getActualState_2_, p_getActualState_3_, EnumFacing.EAST)).withProperty(SOUTH, this.canFenceConnectTo(p_getActualState_2_, p_getActualState_3_, EnumFacing.SOUTH)).withProperty(WEST, this.canFenceConnectTo(p_getActualState_2_, p_getActualState_3_, EnumFacing.WEST));
//  }
}
