package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.Sys;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class BlockOpticalFiber extends BlockTileEntityBase {

  protected static final PropertyBool upConnected = PropertyBool.create("up");
  protected static final PropertyBool downConnected = PropertyBool.create("down");
  protected static final PropertyBool northConnected = PropertyBool.create("north");
  protected static final PropertyBool southConnected = PropertyBool.create("south");
  protected static final PropertyBool eastConnected = PropertyBool.create("east");
  protected static final PropertyBool westConnected = PropertyBool.create("west");
  protected static final PropertyBool isController = PropertyBool.create("controller");


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
            .withProperty(westConnected, false)
            .withProperty(isController, false);
    setDefaultState(defaultState);
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
    return new AxisAlignedBB(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, upConnected, downConnected, northConnected, southConnected, eastConnected, westConnected, isController);
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

  public Boolean isFiberInDirection(IBlockAccess worldIn, BlockPos posIn, EnumFacing direction) {
    return isFiberInPos(worldIn, posIn.offset(direction));
  }

  /**
   * Test if the block at pos is a BlockOpticalFiber
   * @param worldIn The world
   * @param pos The pos
   * @return True iff the block at pos is a BlockOpticalFiber
   */
  private Boolean isFiberInPos(IBlockAccess worldIn, BlockPos pos) {
    return worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiber;
  }

  //TODO remove this and replace with isConnectedTo
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

  /**
   * Get all fibers directly connected to pos
   * @param worldIn The world
   * @param pos Starting pos
   * @param controllerToMatch If not null, ignore fibers with controllers that don't match controllerToMatch
   * @return The set of all fibers directly connected to pos (not including pos)
   */
  Set<BlockPos> getConnectedFibers(IBlockAccess worldIn, BlockPos pos, BlockPos controllerToMatch) {
    Set<BlockPos> res = new HashSet<>(6);
    for (EnumFacing direction : EnumFacing.VALUES) {
      BlockPos testPos = pos.offset(direction);
      if (worldIn.getBlockState(testPos).getBlock() instanceof BlockOpticalFiber) {
        if ((controllerToMatch == null) || (TileOpticalFiberBase.getTileEntity(worldIn, testPos).getControllerPos() == controllerToMatch)) {
          res.add(testPos);
        }
      }
    }
    return res;
  }


  /**
   * Search for all fibers connected to the network including pos
   * @param worldIn The world
   * @param pos Starting pos
   * @param controllerToMatch If not null, ignore fibers with controllers that don't match controllerToMatch
   * @return The set of positions of all connected fibers (including pos)
   */
  public Set<BlockPos> getNetworkedFibers(IBlockAccess worldIn, BlockPos pos, BlockPos controllerToMatch) {
    //TODO parallelize
    Set<BlockPos> complete = Collections.synchronizedSet(new HashSet<>());
    ConcurrentLinkedQueue<BlockPos> frontier = new ConcurrentLinkedQueue<>();

    frontier.offer(pos);

    while (!frontier.isEmpty()) {
      BlockPos newFiber = frontier.poll();
      complete.add(newFiber);
      Set<BlockPos> neighbors = getConnectedFibers(worldIn, newFiber, controllerToMatch);
      neighbors.removeAll(complete);
      frontier.addAll(neighbors);
    }

    return complete;
  }

  /**
   * Test if two fibers are connected by other fibers. Ignores controller information.
   * @param worldIn The world
   * @param pos1 Position of fiber 1
   * @param pos2 Position of fiber 2
   * @return True iff the fibers are connected
   */
  public boolean isNetworkedTo(IBlockAccess worldIn, BlockPos pos1, BlockPos pos2) {
    //TODO parallelize
    Set<BlockPos> complete = Collections.synchronizedSet(new HashSet<>());
    ConcurrentLinkedQueue<BlockPos> frontier = new ConcurrentLinkedQueue<>();

    frontier.offer(pos1);

    while (!frontier.isEmpty()) {
      BlockPos newFiber = frontier.poll();
      if (newFiber.equals(pos2)) {
        return true;
      }
      complete.add(newFiber);
      Set<BlockPos> neighbors = getConnectedFibers(worldIn, newFiber, null);
      neighbors.removeAll(complete);
      frontier.addAll(neighbors);
    }
    return false;
  }

  /**
   * Find the farthest networked fiber by Euclidian distance
   * @param worldIn The world
   * @param pos The position in the network to find the farthest from
   * @param controllerToMatch If not null, ignore fibers with controllers that don't match controllerToMatch
   * @return The position of the farthest networked fiber
   */
  public BlockPos findFarthestNetworked(IBlockAccess worldIn, BlockPos pos, BlockPos controllerToMatch) {
    Set<BlockPos> networked = getNetworkedFibers(worldIn, pos, controllerToMatch);
    double farthestDistance = 0D;
    BlockPos farthestPos = pos;
    for (BlockPos testPos : networked) {
      double distance = testPos.distanceSq(pos);
      if (distance > farthestDistance) {
        farthestDistance = distance;
        farthestPos = testPos;
      }
    }
    return farthestPos;
  }

  /**
   * Removes controller status from an optical fiber
   * @param worldIn The world
   * @param oldControllerPos The position
   * @return The tile entity of the controller
   */
  private TileOpticalFiberController surrenderControllerStatus(World worldIn, BlockPos oldControllerPos, BlockPos newControllerPos) {
    TileOpticalFiberController tile = TileOpticalFiberController.getTileEntity(worldIn, oldControllerPos);
    worldIn.removeTileEntity(oldControllerPos);
    worldIn.setBlockState(oldControllerPos, worldIn.getBlockState(oldControllerPos).withProperty(isController, false));
    worldIn.setTileEntity(oldControllerPos, new TileOpticalFiber(newControllerPos));
    return tile;
  }

  /**
   * Adds controller status to an optical fiber
   * @param worldIn The world
   * @param newControllerPos The position
   * @return The tile entity of the new controller
   */
  private TileOpticalFiberController becomeController(World worldIn, BlockPos newControllerPos) {
    worldIn.removeTileEntity(newControllerPos);
    TileOpticalFiberController newTile = new TileOpticalFiberController();
    worldIn.setTileEntity(newControllerPos, newTile);
    worldIn.setBlockState(newControllerPos, worldIn.getBlockState(newControllerPos).withProperty(isController, true));
    return newTile;
  }

  /**
   * Changes the controller location of all fibers networked to the oldController (excludes oldController)
   * @param worldIn The world
   * @param oldControllerPos The position of the controller to be removed
   * @param newControllerPos The position of the new controller
   */
  private void updateControllerLocations(World worldIn, BlockPos oldControllerPos, BlockPos newControllerPos) {
    Set<BlockPos> secondaryControllerNetwork = getNetworkedFibers(worldIn, oldControllerPos, oldControllerPos);
//    secondaryControllerNetwork.remove(oldControllerPos);
    for (BlockPos networkedPos : secondaryControllerNetwork) {
      TileOpticalFiber.getTileEntity(worldIn, networkedPos).setControllerPos(newControllerPos);
    }
  }

  @Override
  public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
    return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(isController, getConnectedFibers(world, pos, null).isEmpty());
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(World world, IBlockState state) {
    if (state.getValue(isController)) {
      return new TileOpticalFiberController();
    } else {
      // Controller position updated by onBlockPlaced
      return new TileOpticalFiber();
    }
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    if (state.getValue(isController)) {
      // We created a new network containing only ourselves
      return;
    } else {
      TileOpticalFiber tile = TileOpticalFiber.getTileEntity(worldIn, pos);

      // Check state of network
      Set<BlockPos> connectedControllers = new HashSet<>();
      for (EnumFacing direction : EnumFacing.VALUES) {
        if (isFiberInDirection(worldIn, pos, direction)) {
          BlockPos controllerPos = TileOpticalFiberBase.getTileEntity(worldIn, pos.offset(direction)).getControllerPos();
          if (controllerPos == null) {
            FiberOptics.LOGGER.log(Level.WARNING, "No controller stored Optical Fiber.");
            //TODO recover
          }
          connectedControllers.add(controllerPos);
        }
      }

      if (connectedControllers.isEmpty()) {
        // Shouldn't get here in normal gameplay
        FiberOptics.LOGGER.log(Level.WARNING, "No controller stored in Optical Fiber neighbors. Recovering...");
        worldIn.removeTileEntity(pos);
        worldIn.setBlockState(pos, state.withProperty(isController, true));
        TileOpticalFiberController controllerTile = new TileOpticalFiberController();
        worldIn.setTileEntity(pos, controllerTile);
        // Scan through to fix network
        getNetworkedFibers(worldIn, pos, null).forEach((networkedPos) -> {
          if (TileOpticalFiberBase.getTileEntity(worldIn, networkedPos).isController()) {
            TileOpticalFiberController oldControllerTile = surrenderControllerStatus(worldIn, networkedPos, pos);
            //TODO
          } else {
            TileOpticalFiber.getTileEntity(worldIn, networkedPos).setControllerPos(pos);
          }
        });
      } else {
        // Connect to existing network
        Iterator<BlockPos> connectedControllersIterator = connectedControllers.iterator();
        // Choose first connected controller to consolidate into
        BlockPos primaryControllerPos = connectedControllersIterator.next();
        tile.setControllerPos(primaryControllerPos);

        // If connecting multiple networks, consolidate them
        if (connectedControllersIterator.hasNext()) {
          TileOpticalFiberController primaryController = TileOpticalFiberController.getTileEntity(worldIn, primaryControllerPos);
          connectedControllersIterator.forEachRemaining(secondaryControllerPos ->
          {
            TileOpticalFiberController secondaryController = TileOpticalFiberController.getTileEntity(worldIn, secondaryControllerPos);
            surrenderControllerStatus(worldIn, secondaryControllerPos, primaryControllerPos);
            updateControllerLocations(worldIn, secondaryControllerPos, primaryControllerPos);
            primaryController.importData(secondaryController);
          });
        }
      }
    }
  }

  private void getDistinctNetworksHelper(IBlockAccess worldIn, Set<BlockPos> networkedFibers, BlockPos fiber) {
    networkedFibers.add(fiber);
    Set<BlockPos> connectedFibers = getConnectedFibers(worldIn, fiber, null);
    connectedFibers.removeAll(networkedFibers);
    connectedFibers.forEach(connectedFiber -> getDistinctNetworksHelper(worldIn, networkedFibers, connectedFiber));
  }

  /**
   * Gets the distinct networks created when a fiber is destroyed
   * @param worldIn The world
   * @param destroyedBlockPos The destroyed block position
   * @return A maximal list of unnetworked fibers
   */
  private List<Set<BlockPos>> getDistinctNetworks(IBlockAccess worldIn, BlockPos destroyedBlockPos) {
    List<Set<BlockPos>> distinctNetworks = new ArrayList<>();
    Set<BlockPos> startingFibers = getConnectedFibers(worldIn,destroyedBlockPos, null);
    while (!startingFibers.isEmpty()) {
      Set<BlockPos> networkedFibers = new HashSet<>();
      getDistinctNetworksHelper(worldIn, networkedFibers, startingFibers.iterator().next());
      startingFibers.removeAll(networkedFibers);
      distinctNetworks.add(networkedFibers);
    }
    return distinctNetworks;
  }

  private BlockPos getFarthest(Set<BlockPos> posSet, BlockPos relative) {
    BlockPos farthestPos = relative;
    Double farthestDistance = 0D;

    Iterator<BlockPos> posSetIterator = posSet.iterator();
    while (posSetIterator.hasNext()) {
      BlockPos pos = posSetIterator.next();
      double dist = relative.distanceSq(pos);
      if (dist > farthestDistance) {
        farthestPos = pos;
        farthestDistance = dist;
      }
    }
    return farthestPos;
  }

  /**
   * Forms the new networks if necessary when an optical fiber is destroyed
   * @param worldIn The world
   * @param destroyedBlockPos The position of the destroyed optical fiber
   * @param originalController The controller that served the destroyed block
   */
  private void formNewNetworks(World worldIn, BlockPos destroyedBlockPos, TileOpticalFiberController originalController) {
    List<Set<BlockPos>> distinctNetworks = getDistinctNetworks(worldIn, destroyedBlockPos);

    for (Set<BlockPos> distinctNetwork : distinctNetworks) {
      if (originalController.getPos() == destroyedBlockPos || !distinctNetwork.contains(originalController.getPos())) {
        BlockPos farthest = getFarthest(distinctNetwork, destroyedBlockPos);
        TileOpticalFiberController newController = becomeController(worldIn, farthest);
        originalController.splitData(newController, distinctNetwork);
      }
    }
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    if (!getConnectedFibers(worldIn, pos, null).isEmpty()) {
      TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
      TileOpticalFiberController controller = TileOpticalFiberController.getTileEntity(worldIn, tile.getControllerPos());
      controller.removeAllConnectionsForPos(pos);
      formNewNetworks(worldIn, pos, controller);
    }
    super.breakBlock(worldIn, pos, state);
  }
}
