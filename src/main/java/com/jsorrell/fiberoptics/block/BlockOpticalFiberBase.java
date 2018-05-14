package com.jsorrell.fiberoptics.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class BlockOpticalFiberBase<TE> extends BlockTileEntityBase {
  protected static final PropertyBool upConnected = PropertyBool.create("up");
  protected static final PropertyBool downConnected = PropertyBool.create("down");
  protected static final PropertyBool northConnected = PropertyBool.create("north");
  protected static final PropertyBool southConnected = PropertyBool.create("south");
  protected static final PropertyBool eastConnected = PropertyBool.create("east");
  protected static final PropertyBool westConnected = PropertyBool.create("west");


  public BlockOpticalFiberBase(String name) {
    super(Material.ROCK, name);

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
    return state.withProperty(upConnected, canConnectTo(worldIn, pos, EnumFacing.UP))
            .withProperty(downConnected, canConnectTo(worldIn, pos, EnumFacing.DOWN))
            .withProperty(northConnected, canConnectTo(worldIn, pos, EnumFacing.NORTH))
            .withProperty(southConnected, canConnectTo(worldIn, pos, EnumFacing.SOUTH))
            .withProperty(eastConnected, canConnectTo(worldIn, pos, EnumFacing.EAST))
            .withProperty(westConnected, canConnectTo(worldIn, pos, EnumFacing.WEST));
  }

  private Boolean isFiberInPos(IBlockAccess worldIn, BlockPos pos) {
    return worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiberBase;
  }

  private Boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing direction) {
    BlockPos testPos = pos.offset(direction);
    // Connect to other cables
    if (isFiberInPos(worldIn, testPos)) {
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

  Boolean isLoneFiber(World worldIn, BlockPos pos) {
    return !isFiberInPos(worldIn, pos.offset(EnumFacing.UP)) &&
            !isFiberInPos(worldIn, pos.offset(EnumFacing.DOWN)) &&
            !isFiberInPos(worldIn, pos.offset(EnumFacing.NORTH)) &&
            !isFiberInPos(worldIn, pos.offset(EnumFacing.SOUTH)) &&
            !isFiberInPos(worldIn, pos.offset(EnumFacing.EAST)) &&
            !isFiberInPos(worldIn, pos.offset(EnumFacing.WEST));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return 0;
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState();
  }
}
