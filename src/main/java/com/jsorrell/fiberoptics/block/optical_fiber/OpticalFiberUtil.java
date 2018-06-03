package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.utils.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber.getPropertyFromSide;
import static com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber.isController;

public class OpticalFiberUtil {

  /**
   * Finds the fibers connected and adjacent to the fiber at {@code pos}.
   * If there is a connection facing {@code pos}, these fibers are not connected.
   * Used to set the state.
   * @param worldIn the world.
   * @param pos the position.
   * @return the list of sides with adjacent fibers.
   */
  private static List<EnumFacing> findAttachedAdjacentFibers(IBlockAccess worldIn, BlockPos pos) {
    return Arrays.stream(EnumFacing.VALUES)
            .filter(s -> {
              IBlockState state = worldIn.getBlockState(pos.offset(s));
              return state.getBlock() instanceof BlockOpticalFiber && state.getValue(getPropertyFromSide(s.getOpposite())) != FiberSideType.CONNECTION;
            })
            .collect(Collectors.toList());
  }

  /**
   * Checks if a fiber should be placed as a controller.
   * @param worldIn the world.
   * @param pos the position of the fiber to be placed.
   * @return {@code true} iff the fiber should be placed as a controller.
   */
  static boolean shouldBePlacedAsController(IBlockAccess worldIn, BlockPos pos) {
    return findAttachedAdjacentFibers(worldIn, pos).isEmpty();
  }

  /**
   * Get the sides directly connected to another fiber.
   * @param state the state of the block to check.
   * @return the list of all sides of fibers directly connected to the block.
   */
  private static List<EnumFacing> getConnectedSides(IBlockState state) {
    assert state.getBlock() instanceof BlockOpticalFiber;
    List<EnumFacing> res = new ArrayList<>(6);
    Arrays.stream(EnumFacing.VALUES)
            .filter(s -> state.getValue(getPropertyFromSide(s)) == FiberSideType.SELF_ATTACHMENT)
            .forEach(res::add);
    return res;
  }

  /**
   * Get the positions of all fibers directly connected to the fiber at {@code pos}.
   * @param pos the pos to check.
   * @param state the state of the block at pos.
   * @return the list of positions of all fibers directly conncted to the fiber at {@code pos}.
   */
  private static List<BlockPos> getConnectedFibers(BlockPos pos, IBlockState state) {
    assert state.getBlock() instanceof BlockOpticalFiber;
    List<EnumFacing> connectedSides = getConnectedSides(state);
    return connectedSides.stream()
            .map(pos::offset)
            .collect(Collectors.toList());
  }

  /**
   * Removes controller status from an optical fiber.
   * @param worldIn the world.
   * @param oldController the controller to demote.
   * @return the new {@link TileOpticalFiber}
   */
  private static TileOpticalFiber surrenderControllerStatus(World worldIn, TileOpticalFiberController oldController) {
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

  /**
   * Controls the logic to place a fiber in the world.
   * @param worldIn the world.
   * @param pos the position of the newly placed fiber.
   * @param state the state of the newly placed fiber.
   */
  static void placeFiber(World worldIn, BlockPos pos, IBlockState state) {
    if (state.getValue(isController)) {
      // We created a new network containing only ourselves
      assert worldIn.getTileEntity(pos) instanceof TileOpticalFiberController;
    } else {
      // Set the block state to correctly reflect the self attachments
      for (EnumFacing side : findAttachedAdjacentFibers(worldIn, pos)) {
        state = attachFiberOnSide(worldIn, pos, state, side);
      }
      BlockOpticalFiber.setRenderBlockState(worldIn, pos, state);

      // Fix the network information
      TileOpticalFiber tile = Util.getTileChecked(worldIn, pos, TileOpticalFiber.class);

      // Find which controllers we joined
      Stack<BlockPos> connectedControllers = new Stack<>();
      List<BlockPos> attachedFibers = getConnectedFibers(pos, state);
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
   * Sets the block state of the adjacent fiber to attach to this fiber.
   * Returns the new block state for this fiber.
   * DOES NOT CHANGE THE STATE OF THIS FIBER IN THE WORLD.
   * @param worldIn the world.
   * @param pos the position of the fiber in the world.
   * @param state the current state of the fiber.
   * @param side the side of the adjacent fiber to attach to.
   * @return the state the fiber after attaching to the adjacent fiber.
   */
  private static IBlockState attachFiberOnSide(World worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
    assert BlockOpticalFiber.isFiberInPos(worldIn, pos.offset(side));
    worldIn.setBlockState(pos.offset(side), worldIn.getBlockState(pos.offset(side)).withProperty(getPropertyFromSide(side.getOpposite()), FiberSideType.SELF_ATTACHMENT));
    return state.withProperty(getPropertyFromSide(side), FiberSideType.SELF_ATTACHMENT);

  }

  /**
   * Gets the distinct networks created when a fiber is destroyed.
   * @param worldIn the world.
   * @param destroyedBlockPos the destroyed block position.
   * @return a maximal list of positions of unnetworked fibers.
   */
  private static List<Set<BlockPos>> getDistinctNetworks(IBlockAccess worldIn, BlockPos destroyedBlockPos, IBlockState destroyedBlockState) {
    List<Set<BlockPos>> distinctNetworks = new ArrayList<>();
    List<BlockPos> startingFibers = getConnectedFibers(destroyedBlockPos, destroyedBlockState);
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
    List<BlockPos> connectedFibers = getConnectedFibers(fiber, worldIn.getBlockState(fiber));
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
  private static void formNewNetworks(World worldIn, BlockPos destroyedBlockPos, IBlockState destroyedBlockState, TileOpticalFiberController originalController) {
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

  /**
   * When a fiber is broken, changes the controller and removes/splits connections if necessary.
   * @param worldIn the world.
   * @param pos the pos of the fiber.
   * @param tile the tile of the broken fiber.
   */
  static void breakFiber(World worldIn, BlockPos pos, IBlockState state, TileOpticalFiberBase tile) {
    List<EnumFacing> connectedSides = getConnectedSides(state);
    if (!connectedSides.isEmpty()) {
      connectedSides.forEach(s -> BlockOpticalFiber.setSideType(worldIn, pos.offset(s), s.getOpposite(), FiberSideType.NONE));
      TileOpticalFiberController controller = tile.getController();
      controller.removeFiber(tile);
      formNewNetworks(worldIn, pos, state, controller);
    }
  }

  /**
   * Splits a connection between two fibers.
   * @param worldIn the world.
   * @param pos the position of the block to separated.
   * @param side the side to separate.
   */
  public static void splitConnection(World worldIn, BlockPos pos, EnumFacing side) {
    BlockPos otherPos = pos.offset(side);
    BlockOpticalFiber.setSideType(worldIn, pos, side, FiberSideType.NONE);
    BlockOpticalFiber.setSideType(worldIn, otherPos, side.getOpposite(), FiberSideType.NONE);

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
}
