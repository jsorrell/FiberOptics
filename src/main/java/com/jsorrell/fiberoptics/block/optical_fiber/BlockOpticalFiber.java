package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.block.BlockTileEntityBase;
import com.jsorrell.fiberoptics.utils.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

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

  /**
   * Test if the block at pos is a {@link BlockOpticalFiber}.
   * @param worldIn the world.
   * @param pos the pos.
   * @return {@code true} iff the block at pos is a {@link BlockOpticalFiber}.
   */
  public static boolean isFiberInPos(IBlockAccess worldIn, BlockPos pos) {
    return worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiber;
  }

  private Set<EnumFacing> findAdjacentFibers(IBlockAccess worldIn, BlockPos pos) {
    return Arrays.stream(EnumFacing.VALUES)
            .filter(s -> isFiberInPos(worldIn, pos.offset(s)))
            .filter(s -> {
              IBlockState state = worldIn.getBlockState(pos.offset(s));
              return state.getBlock() instanceof BlockOpticalFiber && state.getValue(getPropertyFromSide(s.getOpposite())) != FiberSideType.CONNECTION;
            })
            .collect(Collectors.toSet());
  }

  /**
   * Get the sides directly connected to another fiber.
   * @param state the state of the block to check.
   * @return the set of all sides of fibers directly connected to the block.
   */
  private static Set<EnumFacing> getConnectedSides(IBlockState state) {
    assert state.getBlock() instanceof BlockOpticalFiber;
    Set<EnumFacing> res = new HashSet<>(6);
    Arrays.stream(EnumFacing.VALUES)
            .filter(s -> state.getValue(getPropertyFromSide(s)) == FiberSideType.SELF_ATTACHMENT)
            .forEach(res::add);
    return res;
  }

  /**
   * Get the positions of all fibers directly connected to the fiber at {@code pos}.
   * @param pos the pos to check.
   * @param state the state of the block at pos.
   * @return the set positions of all fibers directly conncted to the fiber at {@code pos}.
   */
  private static Set<BlockPos> getConnectedFibers(BlockPos pos, IBlockState state) {
    assert state.getBlock() instanceof BlockOpticalFiber;
    Set<EnumFacing> connectedSides = getConnectedSides(state);
    return connectedSides.stream()
            .map(pos::offset)
            .collect(Collectors.toSet());
  }

  /**
   * Removes controller status from an optical fiber.
   * @param worldIn the world.
   * @param oldController the controller to demote.
   * @return the new {@link TileOpticalFiber}
   */
  private TileOpticalFiber surrenderControllerStatus(World worldIn, TileOpticalFiberController oldController) {
    worldIn.setBlockState(oldController.getPos(), worldIn.getBlockState(oldController.getPos()).withProperty(isController, false), 2|4|16);
    TileOpticalFiber newTile = Util.getTileChecked(worldIn, oldController.getPos(), TileOpticalFiber.class);
    newTile.importConnections(oldController);
    return newTile;
  }

  /**
   * Adds controller status to an optical fiber.
   * @param worldIn the world.
   * @param oldFiber the fiber to promote.
   * @return the new {@link TileOpticalFiberController}.
   */
  private static TileOpticalFiberController becomeController(World worldIn, TileOpticalFiber oldFiber) {
    worldIn.setBlockState(oldFiber.getPos(), worldIn.getBlockState(oldFiber.getPos()).withProperty(isController, true), 2|4|16);
    TileOpticalFiberController newController = Util.getTileChecked(worldIn, oldFiber.getPos(), TileOpticalFiberController.class);
    newController.importConnections(oldFiber);
    assert newController.getNetworkBlocks().contains(oldFiber.getPos());
    return newController;
  }

  @Nonnull
  @Override
  public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand) {
    IBlockState superState = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    // Find see if any fibers without connections facing us
    boolean hasAdjacent = Arrays.stream(EnumFacing.VALUES)
            .anyMatch(s -> isFiberInPos(world, pos.offset(s))
                    && world.getBlockState(pos.offset(s)).getValue(getPropertyFromSide(s.getOpposite())) != FiberSideType.CONNECTION);
    // If there are no connected fibers, this is placed as a controller
    return superState.withProperty(isController, !hasAdjacent);
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

  /**
   * Sets the block state of the adjacent fiber to attach to this fiber.
   * Returns the new block state for this fiber.
   * DOES NOT CHANGE THE STATE OF THIS FIBER IN THE WORLD.
   * @param worldIn the world.
   * @param pos the position of the fiber in the world.
   * @param state the current state of the fiber.
   * @param side the side of the adjacent fiber to attach to.
   * @return the state the fiber after attaching to the adjacent fiber.
   */
  private IBlockState attachFiberOnSide(World worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    assert isFiberInPos(worldIn, pos.offset(side));
    worldIn.setBlockState(pos.offset(side), worldIn.getBlockState(pos.offset(side)).withProperty(getPropertyFromSide(side.getOpposite()), FiberSideType.SELF_ATTACHMENT));
    return state.withProperty(getPropertyFromSide(side), FiberSideType.SELF_ATTACHMENT);

  }

  @Override
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    placeFiber(worldIn, pos, state);
    super.onBlockAdded(worldIn, pos, state);
  }

  private void placeFiber(World worldIn, BlockPos pos, IBlockState state) {
    if (state.getValue(isController)) {
      // We created a new network containing only ourselves
      assert worldIn.getTileEntity(pos) instanceof TileOpticalFiberController;
    } else {
      // Set the block state to correctly reflect the self attachments
      for (EnumFacing side : findAdjacentFibers(worldIn, pos)) {
        state = attachFiberOnSide(worldIn, pos, state, side);
      }
      setRenderBlockState(worldIn, pos, state);

      // Fix the network information
      TileOpticalFiber tile = Util.getTileChecked(worldIn, pos, TileOpticalFiber.class);

      // Find which controllers we joined
      Stack<BlockPos> connectedControllers = new Stack<>();
      Set<BlockPos> attachedFibers = getConnectedFibers(pos, state);
      for (BlockPos pos1 : attachedFibers) {
        BlockPos conPos = Util.getTileChecked(worldIn, pos1, TileOpticalFiberBase.class).getControllerPos();
        if (!connectedControllers.contains(conPos)) connectedControllers.push(conPos);
      }
      assert (!connectedControllers.empty());

      // Connect to existing network
      // Choose first connected controller to consolidate into
      TileOpticalFiberController primaryController = Util.getTileChecked(worldIn, connectedControllers.pop(), TileOpticalFiberController.class);
      primaryController.addFiber(tile);

      // If connecting multiple networks, consolidate them
      while (!connectedControllers.empty()) {
        TileOpticalFiberController secondaryController = Util.getTileChecked(worldIn, connectedControllers.pop(), TileOpticalFiberController.class);
        surrenderControllerStatus(worldIn, secondaryController);
        primaryController.cannibalize(secondaryController);
      }
    }
  }

  /**
   * Gets the distinct networks created when a fiber is destroyed.
   * @param worldIn the world.
   * @param destroyedBlockPos the destroyed block position.
   * @return a maximal list of positions of unnetworked fibers.
   */
  private List<Set<BlockPos>> getDistinctNetworks(IBlockAccess worldIn, BlockPos destroyedBlockPos, IBlockState destroyedBlockState) {
    List<Set<BlockPos>> distinctNetworks = new ArrayList<>();
    Set<BlockPos> startingFibers = getConnectedFibers(destroyedBlockPos, destroyedBlockState);
    while (!startingFibers.isEmpty()) {
      Set<BlockPos> networkedFibers = getConnectedBlocks(worldIn, startingFibers.iterator().next());
      startingFibers.removeAll(networkedFibers);
      distinctNetworks.add(networkedFibers);
    }
    return distinctNetworks;
  }

  /**
   * Finds every connected block in the network.
   * @param world the world.
   * @param startingPos the position to search from and find blocks connected to.
   * @return the set of positions of all connected blocks in the network.
   */
  private static Set<BlockPos> getConnectedBlocks(IBlockAccess world, BlockPos startingPos) {
    Set<BlockPos> networkedFibers = new HashSet<>();
    getConnectedBlocksHelper(world, startingPos, networkedFibers);
    return networkedFibers;
  }

  private static void getConnectedBlocksHelper(IBlockAccess worldIn, BlockPos fiber, Set<BlockPos> networkedFibers) {
    networkedFibers.add(fiber);
    Set<BlockPos> connectedFibers = getConnectedFibers(fiber, worldIn.getBlockState(fiber));
    connectedFibers.removeAll(networkedFibers);
    connectedFibers.forEach(connectedFiber -> getConnectedBlocksHelper(worldIn, connectedFiber, networkedFibers));
  }

  /**
   * Gets the farthest {@link BlockPos} in {@code posSet} from {@code relative} by Euclidean position.
   * @param posSet the set of positions to search.
   * @param relative the position to measure relative to.
   * @return the farthest position from {@code relative}.
   */
  private static BlockPos getFarthest(Set<BlockPos> posSet, BlockPos relative) {
    BlockPos farthestPos = relative;
    Double farthestDistance = 0D;

    for (BlockPos pos : posSet) {
      double dist = relative.distanceSq(pos);
      if (dist > farthestDistance) {
        farthestPos = pos;
        farthestDistance = dist;
      }
    }
    return farthestPos;
  }

  /**
   * Forms the new networks if necessary when an optical fiber is destroyed.
   * Performs a search of the whole network.
   * @param worldIn the world.
   * @param destroyedBlockPos the position of the destroyed optical fiber.
   * @param originalController the controller that served the destroyed block.
   */
  private void formNewNetworks(World worldIn, BlockPos destroyedBlockPos, IBlockState destroyedBlockState, TileOpticalFiberController originalController) {
    List<Set<BlockPos>> distinctNetworks = getDistinctNetworks(worldIn, destroyedBlockPos, destroyedBlockState);
    boolean blockDestroyedWasController = originalController.getPos().equals(destroyedBlockPos);

    for (Set<BlockPos> distinctNetwork : distinctNetworks) {
      if (blockDestroyedWasController || !distinctNetwork.contains(originalController.getPos())) {
        BlockPos farthest = getFarthest(distinctNetwork, destroyedBlockPos);
        TileOpticalFiberController newController = becomeController(worldIn, Util.getTileChecked(worldIn, farthest, TileOpticalFiber.class));
        originalController.migrateFibersTo(newController, distinctNetwork);
      }
    }
    assert blockDestroyedWasController == originalController.getNetworkBlocks().isEmpty();
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileOpticalFiberBase tile = Util.getTileChecked(worldIn, pos, TileOpticalFiberBase.class);
    breakFiber(worldIn, pos, state, tile);
    super.breakBlock(worldIn, pos, state);
  }

  /**
   * When a fiber is broken, changes the controller and removes/splits connections if necessary.
   * @param worldIn the world.
   * @param pos the pos of the fiber.
   * @param tile the tile of the broken fiber.
   */
  private void breakFiber(World worldIn, BlockPos pos, IBlockState state, TileOpticalFiberBase tile) {
    Set<EnumFacing> connectedSides = getConnectedSides(state);
    if (!connectedSides.isEmpty()) {
      connectedSides.forEach(s -> setSideType(worldIn, pos.offset(s), s.getOpposite(), FiberSideType.NONE));
      TileOpticalFiberController controller = tile.getController();
      controller.removeFiber(tile);
      formNewNetworks(worldIn, pos, state, controller);
    }

    worldIn.removeTileEntity(pos);
  }

  /**
   * Splits a connection between two fibers.
   * @param worldIn the world.
   * @param pos the position of the block to separated.
   * @param side the side to separate.
   */
  public static void splitConnection(World worldIn, BlockPos pos, EnumFacing side) {
    BlockPos otherPos = pos.offset(side);
    setSideType(worldIn, pos, side, FiberSideType.NONE);
    setSideType(worldIn, otherPos, side.getOpposite(), FiberSideType.NONE);

    Set<BlockPos> posNetwork = getConnectedBlocks(worldIn, pos);
    if (!posNetwork.contains(otherPos)) {
      // We separated two networks
      TileOpticalFiberBase posTile = Util.getTileChecked(worldIn, pos, TileOpticalFiberBase.class);

      Set<BlockPos> controllerNetwork;
      Set<BlockPos> otherNetwork;
      if (posNetwork.contains(posTile.getControllerPos())) {
        controllerNetwork = posNetwork;
        otherNetwork = posTile.getNetworkBlocks();
        otherNetwork.removeAll(controllerNetwork);
      } else {
        otherNetwork = posNetwork;
        controllerNetwork = posTile.getNetworkBlocks();
        controllerNetwork.removeAll(otherNetwork);
      }

      assert controllerNetwork.contains(posTile.getControllerPos());
      assert Collections.disjoint(controllerNetwork, otherNetwork);

      TileOpticalFiberController originalController = posTile.getController();

      BlockPos newControllerPos = getFarthest(otherNetwork, pos);
      assert otherNetwork.contains(newControllerPos);
      TileOpticalFiberController newController = becomeController(worldIn, Util.getTileChecked(worldIn, newControllerPos, TileOpticalFiber.class));
      originalController.migrateFibersTo(newController, otherNetwork);
    }
  }

  static void setSideType(World world, BlockPos pos, EnumFacing side, FiberSideType type) {
    IBlockState state = world.getBlockState(pos);
    state = state.withProperty(getPropertyFromSide(side), type);
    setRenderBlockState(world, pos, state);
  }
  static void setRenderBlockState(World world, BlockPos pos, IBlockState state) {
    if (!world.getBlockState(pos).equals(state)) {
      world.setBlockState(pos, state, 2 | 16);
    }
  }
}
