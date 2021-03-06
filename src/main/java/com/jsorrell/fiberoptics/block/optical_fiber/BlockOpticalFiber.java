package com.jsorrell.fiberoptics.block.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.block.BlockTileEntityBase;
import com.jsorrell.fiberoptics.item.Terminator;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenSideChooser;
import com.jsorrell.fiberoptics.util.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockOpticalFiber extends BlockTileEntityBase {

  protected static final PropertyFiberSide down = PropertyFiberSide.create("down");
  protected static final PropertyFiberSide up = PropertyFiberSide.create("up");
  protected static final PropertyFiberSide north = PropertyFiberSide.create("north");
  protected static final PropertyFiberSide south = PropertyFiberSide.create("south");
  protected static final PropertyFiberSide west = PropertyFiberSide.create("west");
  protected static final PropertyFiberSide east = PropertyFiberSide.create("east");
  protected static final PropertyBool isController = PropertyBool.create("controller");

  /**
   * Set in {@link this#drawSelectedBoundingBox(DrawBlockHighlightEvent)}.
   * Used in {@link this#getSelectedBoundingBox(IBlockState, World, BlockPos)}.
   */
  private AxisAlignedBB selectedBoundingBox = null;

  public static PropertyFiberSide getPropertyFromSide(EnumFacing side) {
    switch (side) {
      case DOWN: return down;
      case UP: return up;
      case NORTH: return north;
      case SOUTH: return south;
      case WEST: return west;
      case EAST: return east;
    }
    throw new AssertionError("Should never get here.");
  }

  public BlockOpticalFiber() {
    super(Material.ROCK, "optical_fiber");

    setHardness(3f);
    setResistance(5000f);
    setLightOpacity(0);

    IBlockState defaultState = this.blockState.getBaseState()
            .withProperty(down, FiberSideType.NONE)
            .withProperty(up, FiberSideType.NONE)
            .withProperty(north, FiberSideType.NONE)
            .withProperty(south, FiberSideType.NONE)
            .withProperty(west, FiberSideType.NONE)
            .withProperty(east, FiberSideType.NONE)
            .withProperty(isController, false);
    setDefaultState(defaultState);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(isController, (meta & 0x1) == 0x1);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    int meta = 0;
    if (state.getValue(isController)) {
      meta |= 0x1;
    }
    return meta;
  }

  @Override
  @Deprecated
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  @Deprecated
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  @Deprecated
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return Arrays.stream(EnumFacing.VALUES)
            .map(s -> getBoundingBoxForPart(state, FiberPart.fromSide(s)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(getBoundingBoxForCenter(), AxisAlignedBB::union);
  }

  @Nullable
  @Override
  public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
    Vec3d relStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
    Vec3d relEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());

    RayTraceResult blockCollision = null;
    FiberPart hitPart = null;
    Vec3d hitPos = null;

    for (FiberPart part : FiberPart.values()) {
      Optional<AxisAlignedBB> bbOpt = getBoundingBoxForPart(blockState, part);
      if (!bbOpt.isPresent()) continue;
      AxisAlignedBB bb = bbOpt.get();
      RayTraceResult collision = bb.calculateIntercept(relStart, relEnd);
      if (collision != null && (blockCollision == null || collision.hitVec.distanceTo(relStart) < blockCollision.hitVec.distanceTo(relStart))) {
        blockCollision = collision;
        hitPart = part;
        hitPos = bb.getCenter(); // We set the hit position to the center so that later we can tell which part was hit
      }
    }

    if (blockCollision == null) {
      return null;
    } else {
      RayTraceResult res = new RayTraceResult(hitPos.addVector(pos.getX(), pos.getY(), pos.getZ()), blockCollision.sideHit, pos);
      res.subHit = hitPart.getIndex();
      return res;
    }
  }

  public static AxisAlignedBB getBoundingBoxForSelfAttachment(EnumFacing side) {
    return getBoundingBoxForConnection(side);
  }

  public static AxisAlignedBB getBoundingBoxForConnection(EnumFacing side) {
    switch (side) {
      case DOWN:
        return new AxisAlignedBB(0.375D, 0D, 0.375D, 0.625D, 0.375D, 0.625D);
      case UP:
        return new AxisAlignedBB(0.375D, 0.625D, 0.375D, 0.625D, 1D, 0.625D);
      case NORTH:
        return new AxisAlignedBB(0.375D, 0.375D, 0D, 0.625D, 0.625D, 0.375D);
      case SOUTH:
        return new AxisAlignedBB(0.375D, 0.375D, 0.625D, 0.625D, 0.625D, 1D);
      case WEST:
        return new AxisAlignedBB(0D, 0.375D, 0.375D, 0.375D, 0.625D, 0.625D);
      case EAST:
        return new AxisAlignedBB(0.625D, 0.375D, 0.375D, 1D, 0.625D, 0.625D);
    }
    throw new AssertionError("Should never get here.");
  }

  public static AxisAlignedBB getBoundingBoxForCenter() {
    return new AxisAlignedBB(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
  }

  public static Optional<AxisAlignedBB> getBoundingBoxForPart(IBlockState state, FiberPart part) {
    if (part.getSide().isPresent()) {
      EnumFacing side = part.getSide().get();
      FiberSideType fiberSideType = state.getValue(getPropertyFromSide(side));
      if (fiberSideType == FiberSideType.CONNECTION) {
        return Optional.of(getBoundingBoxForConnection(side));
      } else if (fiberSideType == FiberSideType.SELF_ATTACHMENT) {
        return Optional.of(getBoundingBoxForSelfAttachment(side));
      } else if (fiberSideType == FiberSideType.NONE) {
        return Optional.empty();
      } else {
        throw new AssertionError("Should never get here.");
      }
    } else {
      return Optional.of(getBoundingBoxForCenter());
    }
  }

  public static Optional<AxisAlignedBB> getSelectedBoxForPart(IBlockState state, FiberPart part) {
    if (part.getSide().isPresent()) {
      EnumFacing side = part.getSide().get();
      FiberSideType fiberSideType = state.getValue(getPropertyFromSide(side));
      if (fiberSideType == FiberSideType.SELF_ATTACHMENT) {
        return Optional.of(getBoundingBoxForSelfAttachment(side).union(getBoundingBoxForSelfAttachment(side.getOpposite()).offset(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ())));
      }
    }
    return getBoundingBoxForPart(state, part);
  }

  @Override
  public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
    BlockOpticalFiber.addCollisionBoxToList(pos, entityBox, collidingBoxes, getBoundingBoxForCenter());
    Arrays.stream(EnumFacing.VALUES)
            .map(s -> getBoundingBoxForPart(state, FiberPart.fromSide(s)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(bb -> BlockOpticalFiber.addCollisionBoxToList(pos, entityBox, collidingBoxes, bb));
  }

  @Override
  public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
    assert this.selectedBoundingBox != null;
    return this.selectedBoundingBox;
  }

  @SideOnly(Side.CLIENT)
  @SuppressWarnings({"unused", "WeakerAccess"})
  @SubscribeEvent (priority = EventPriority.LOW)
  public void drawSelectedBoundingBox(DrawBlockHighlightEvent e) {
    if(e.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
      World world = e.getPlayer().world;
      BlockPos pos = e.getTarget().getBlockPos();
      IBlockState state = world.getBlockState(pos);
      Block block = state.getBlock();

      if (block instanceof BlockOpticalFiber) {
        FiberPart part = FiberPart.fromIndex(e.getTarget().subHit);
        Optional<AxisAlignedBB> boxOpt = getSelectedBoxForPart(state, part);
        if (boxOpt.isPresent()) {
          AxisAlignedBB box = boxOpt.get();
         ((BlockOpticalFiber) block).selectedBoundingBox = box.offset(pos);
          e.getContext().drawSelectionBox(e.getPlayer(), e.getTarget(), 0, e.getPartialTicks());
          e.setCanceled(true);
        }
      }
    }
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, down, up, north, south, west, east, isController);
  }

  @Nonnull
  @Override
  public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand) {
    IBlockState superState = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    // If there are no adjacent fibers, this is placed as a controller
    return superState.withProperty(isController, OpticalFiberUtil.shouldBePlacedAsController(world, pos));
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    if (world.isRemote) {
      return new TileOpticalFiberClient();
    }

    if (state.getValue(isController)) {
      return new TileOpticalFiberController();
    } else {
      // Controller position updated by onBlockPlaced
      return new TileOpticalFiber();
    }
  }

  @Override
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    OpticalFiberUtil.placeFiber(worldIn, pos, state);
    super.onBlockAdded(worldIn, pos, state);
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileOpticalFiberBase tile = Util.getTileChecked(worldIn, pos, TileOpticalFiberBase.class);
    OpticalFiberUtil.breakFiber(worldIn, pos, state, tile);
    worldIn.removeTileEntity(pos);
    super.breakBlock(worldIn, pos, state);
  }

  /**
   * Test if the block at pos is a {@link BlockOpticalFiber}.
   * @param worldIn the world.
   * @param pos the pos.
   * @return {@code true} iff the block at pos is a {@link BlockOpticalFiber}.
   */
  public static boolean isFiberInPos(IBlockAccess worldIn, BlockPos pos) {
    return worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiber;
  }

  /**
   * Sets the type of connection on the side.
   * @param world the world.
   * @param pos the position of the connection to set the side of.
   * @param side the side to set.
   * @param type the type to set the side to.
   */
  static void setSideType(World world, BlockPos pos, EnumFacing side, FiberSideType type) {
    IBlockState state = world.getBlockState(pos);
    state = state.withProperty(getPropertyFromSide(side), type);
    setRenderBlockState(world, pos, state);
  }

  /**
   * Sets the block state and sends to client.
   * @param world the world.
   * @param pos the position of the block.
   * @param state the state to set the block to.
   */
  static void setRenderBlockState(World world, BlockPos pos, IBlockState state) {
    if (!world.getBlockState(pos).equals(state)) {
      world.setBlockState(pos, state, 2 | 16);
    }
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  @SubscribeEvent (priority = EventPriority.HIGH)
  public void handleRightClick(PlayerInteractEvent.RightClickBlock e) {
    if (!e.getWorld().isRemote) {
      IBlockState state = e.getWorld().getBlockState(e.getPos());
      assert e.getFace() != null; // Not null for RightClockBlock
      BlockPos adjacentPos = e.getPos().offset(e.getFace());
      IBlockState adjacentState = e.getWorld().getBlockState(adjacentPos);
      Block adjacentBlock = adjacentState.getBlock();

      if (state.getBlock() instanceof BlockOpticalFiber) {
        if (e.getEntityPlayer().getHeldItem(e.getHand()).getItem() instanceof Terminator) {
          if (e.getEntityPlayer().isSneaking()) {
            FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenSideChooser(e.getPos()), (EntityPlayerMP) e.getEntityPlayer());
          } else {
            Vec3d hitVec = e.getHitVec().subtract(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ());
            if (BlockOpticalFiber.getBoundingBoxForCenter().contains(hitVec)) {
              /* HIT CENTER */
              FiberSideType faceType = state.getValue(BlockOpticalFiber.getPropertyFromSide(e.getFace()));
              if (adjacentBlock instanceof BlockOpticalFiber) {
                /* There is another fiber on the side we hit. Try to join the fibers. */
                assert faceType == FiberSideType.NONE; // We shouldn't be able to hit a face of the center unless that face doesn't have a connection.
                FiberSideType adjacentFaceType = adjacentState.getValue(BlockOpticalFiber.getPropertyFromSide(e.getFace().getOpposite()));
                if (adjacentFaceType != FiberSideType.CONNECTION) {
                  OpticalFiberUtil.joinConnection(e.getWorld(), e.getPos(), e.getFace());
                }
              } else {
                /* There is not another fiber on the side we hit. Make a connection */
                TileOpticalFiberBase tile = Util.getTileChecked(e.getWorld(), e.getPos(), TileOpticalFiberBase.class);
                FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(e.getPos(), ImmutableList.of(e.getFace()), tile.getConnections(e.getFace())), (EntityPlayerMP) e.getEntityPlayer());
              }
            } else {
              /* HIT ONE OF THE SIDES */
              for (EnumFacing side : EnumFacing.VALUES) {
                Optional<AxisAlignedBB> boxOpt = BlockOpticalFiber.getBoundingBoxForPart(state, FiberPart.fromSide(side));
                if (boxOpt.isPresent() && boxOpt.get().contains(hitVec)) {
                  FiberSideType sideType = state.getValue(BlockOpticalFiber.getPropertyFromSide(side));
                  if (sideType == FiberSideType.SELF_ATTACHMENT) {
                    /* Hit a self attachment: Split it. */
                    OpticalFiberUtil.splitConnection(e.getWorld(), e.getPos(), side);
                  } else {
                    /* Hit a connection: Try to add another to this side. */
                    assert sideType == FiberSideType.CONNECTION;
                    TileOpticalFiberBase tile = Util.getTileChecked(e.getWorld(), e.getPos(), TileOpticalFiberBase.class);
                    FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(e.getPos(), ImmutableList.of(side), tile.getConnections(side)), (EntityPlayerMP) e.getEntityPlayer());
                  }
                }
              }
            }
          }
        }
        //TODO change tile entity check to checking if we can add connection or get rid of entirely so that we can edit/remove connections facing a non-tile-entity.
      } else if (adjacentBlock instanceof BlockOpticalFiber && state.getBlock().hasTileEntity(state)) {
        if (e.getEntityPlayer().isSneaking()) {
          /* Hit a tile face adjacent to a fiber while sneaking */
          TileOpticalFiberBase tile = Util.getTileChecked(e.getWorld(), adjacentPos, TileOpticalFiberBase.class);
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(adjacentPos, ImmutableList.of(e.getFace().getOpposite()), tile.getConnections(e.getFace().getOpposite())), (EntityPlayerMP) e.getEntityPlayer());
          e.setUseBlock(Event.Result.DENY);
        }
      }
    }
  }
}
