package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.BlockTileEntityBase;
import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiConnectionChooser;
import com.jsorrell.fiberoptics.client.gui.optical_fiber.GuiSideChooser;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.item.ModItems;
import com.jsorrell.fiberoptics.item.Terminator;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenSideChooser;
import com.sun.xml.internal.ws.api.pipe.Fiber;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.Sys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
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
  @Nonnull
  public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    boolean directions[] = {
            isFiberInPos(worldIn, pos.offset(EnumFacing.DOWN)),
            isFiberInPos(worldIn, pos.offset(EnumFacing.UP)),
            isFiberInPos(worldIn, pos.offset(EnumFacing.NORTH)),
            isFiberInPos(worldIn, pos.offset(EnumFacing.SOUTH)),
            isFiberInPos(worldIn, pos.offset(EnumFacing.WEST)),
            isFiberInPos(worldIn, pos.offset(EnumFacing.EAST))
    };

    TileEntity tile = worldIn instanceof ChunkCache ? ((ChunkCache) worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
    List<OpticalFiberConnection> connections;
    if (tile instanceof TileOpticalFiberBase) {
      connections = ((TileOpticalFiberBase) tile).getConnections();
    } else {
      // TODO get connected directions with packet
      connections = new ArrayList<>();
    }

    for (OpticalFiberConnection connection : connections) {
      directions[connection.connectedSide.getIndex()] = true;
    }

    return state.withProperty(downConnected, directions[EnumFacing.DOWN.getIndex()])
            .withProperty(upConnected, directions[EnumFacing.UP.getIndex()])
            .withProperty(northConnected, directions[EnumFacing.NORTH.getIndex()])
            .withProperty(southConnected, directions[EnumFacing.SOUTH.getIndex()])
            .withProperty(westConnected, directions[EnumFacing.WEST.getIndex()])
            .withProperty(eastConnected, directions[EnumFacing.EAST.getIndex()]);
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
   * Get all fibers directly connected to pos.
   * @param worldIn the world.
   * @param pos position to find fibers connected to.
   * @return The set of all positions of {@link BlockOpticalFiber}s directly connected to {@code pos} (not including {@code pos}).
   */
  Set<BlockPos> getConnectedFibers(IBlockAccess worldIn, BlockPos pos) {
    Set<BlockPos> res = new HashSet<>(6);
    for (EnumFacing direction : EnumFacing.VALUES) {
      BlockPos testPos = pos.offset(direction);
      if (worldIn.getBlockState(testPos).getBlock() instanceof BlockOpticalFiber) {
        res.add(testPos);
      }
    }
    return res;
  }

  /**
   * Removes controller status from an optical fiber.
   * @param worldIn the world.
   * @param oldController the controller to demote.
   * @return the new {@link TileOpticalFiber}
   */
  private TileOpticalFiber surrenderControllerStatus(World worldIn, TileOpticalFiberController oldController) {
    worldIn.setBlockState(oldController.getPos(), worldIn.getBlockState(oldController.getPos()).withProperty(isController, false), 2|4|16);
    TileOpticalFiber newTile = TileOpticalFiber.getTileEntity(worldIn, oldController.getPos());
    newTile.importConnections(oldController);
    return newTile;
  }

  /**
   * Adds controller status to an optical fiber.
   * @param worldIn the world.
   * @param oldFiber the fiber to promote.
   * @return the new {@link TileOpticalFiberController}.
   */
  private TileOpticalFiberController becomeController(World worldIn, TileOpticalFiber oldFiber) {
    worldIn.setBlockState(oldFiber.getPos(), worldIn.getBlockState(oldFiber.getPos()).withProperty(isController, true), 2|4|16);
    TileOpticalFiberController newController = TileOpticalFiberController.getTileEntity(worldIn, oldFiber.getPos());
    newController.importConnections(oldFiber);
    assert newController.getNetworkBlocks().contains(oldFiber.getPos());
    return newController;
  }

  @Nonnull
  @Override
  public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand) {
    IBlockState superState = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    // If there are no connected fibers, this is placed as a controller
    return superState.withProperty(isController, getConnectedFibers(world, pos).isEmpty());
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
    if (world.isRemote) return null;
    if (state.getValue(isController)) {
      return new TileOpticalFiberController();
    } else {
      // Controller position updated by onBlockPlaced
      return new TileOpticalFiber();
    }
  }

//  @Override
//  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
//    if (state.getValue(isController)) {
//      // We created a new network containing only ourselves
//      assert worldIn.getTileEntity(pos) instanceof TileOpticalFiberController;
//    } else {
//      TileOpticalFiber tile = TileOpticalFiber.getTileEntity(worldIn, pos);
//
//      // Find which controllers we joined
//      Stack<TileOpticalFiberController> connectedControllers = new Stack<>();
//      for (EnumFacing direction : EnumFacing.VALUES) {
//        if (isFiberInPos(worldIn, pos.offset(direction))) {
//          TileOpticalFiberController con = TileOpticalFiberBase.getTileEntity(worldIn, pos.offset(direction)).getController();
//          if (!connectedControllers.contains(con)) connectedControllers.push(con);
//        }
//      }
//      assert (!connectedControllers.empty());
//
//      // Connect to existing network
//      // Choose first connected controller to consolidate into
//      TileOpticalFiberController primaryController = connectedControllers.pop();
//      primaryController.addFiber(tile);
//
//      // If connecting multiple networks, consolidate them
//      while (!connectedControllers.empty()) {
//          TileOpticalFiberController secondaryController = connectedControllers.pop();
//          assert secondaryController.getNetworkBlocks().contains(secondaryController.getPos());
//          surrenderControllerStatus(worldIn, secondaryController);
//        assert secondaryController.getNetworkBlocks().contains(secondaryController.getPos());
//          primaryController.cannibalize(secondaryController);
//      }
//    }
//  }

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
      TileOpticalFiber tile = TileOpticalFiber.getTileEntity(worldIn, pos);

      // Find which controllers we joined
      Stack<TileOpticalFiberController> connectedControllers = new Stack<>();
      for (EnumFacing direction : EnumFacing.VALUES) {
        if (isFiberInPos(worldIn, pos.offset(direction))) {
          TileOpticalFiberController con = TileOpticalFiberBase.getTileEntity(worldIn, pos.offset(direction)).getController();
          if (!connectedControllers.contains(con)) connectedControllers.push(con);
        }
      }
      assert (!connectedControllers.empty());

      // Connect to existing network
      // Choose first connected controller to consolidate into
      TileOpticalFiberController primaryController = connectedControllers.pop();
      primaryController.addFiber(tile);

      // If connecting multiple networks, consolidate them
      while (!connectedControllers.empty()) {
        TileOpticalFiberController secondaryController = connectedControllers.pop();
        assert secondaryController.getNetworkBlocks().contains(secondaryController.getPos());
        surrenderControllerStatus(worldIn, secondaryController);
        assert secondaryController.getNetworkBlocks().contains(secondaryController.getPos());
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
  private List<Set<BlockPos>> getDistinctNetworks(IBlockAccess worldIn, BlockPos destroyedBlockPos) {
    List<Set<BlockPos>> distinctNetworks = new ArrayList<>();
    Set<BlockPos> startingFibers = getConnectedFibers(worldIn, destroyedBlockPos);
    while (!startingFibers.isEmpty()) {
      Set<BlockPos> networkedFibers = new HashSet<>();
      getDistinctNetworksHelper(worldIn, networkedFibers, startingFibers.iterator().next());
      startingFibers.removeAll(networkedFibers);
      distinctNetworks.add(networkedFibers);
    }
    return distinctNetworks;
  }

  private void getDistinctNetworksHelper(IBlockAccess worldIn, Set<BlockPos> networkedFibers, BlockPos fiber) {
    networkedFibers.add(fiber);
    Set<BlockPos> connectedFibers = getConnectedFibers(worldIn, fiber);
    connectedFibers.removeAll(networkedFibers);
    connectedFibers.forEach(connectedFiber -> getDistinctNetworksHelper(worldIn, networkedFibers, connectedFiber));
  }

  /**
   * Gets the farthest {@link BlockPos} in {@code posSet} from {@code relative} by Euclidean position.
   * @param posSet the set of positions to search.
   * @param relative the position to measure relative to.
   * @return the farthest position from {@code relative}.
   */
  private BlockPos getFarthest(Set<BlockPos> posSet, BlockPos relative) {
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
  private void formNewNetworks(World worldIn, BlockPos destroyedBlockPos, TileOpticalFiberController originalController) {
    List<Set<BlockPos>> distinctNetworks = getDistinctNetworks(worldIn, destroyedBlockPos);
    boolean blockDestroyedWasController = originalController.getPos().equals(destroyedBlockPos);

    for (Set<BlockPos> distinctNetwork : distinctNetworks) {
      if (blockDestroyedWasController || !distinctNetwork.contains(originalController.getPos())) {
        BlockPos farthest = getFarthest(distinctNetwork, destroyedBlockPos);
        TileOpticalFiberController newController = becomeController(worldIn, TileOpticalFiber.getTileEntity(worldIn, farthest));
        for (BlockPos pos : distinctNetwork) {
          // Move to the new network
          TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
          boolean success = originalController.removeFiber(tile);
          assert success;
          if (!pos.equals(newController.getPos())) {
            newController.addFiber((TileOpticalFiber) tile);
          }
        }
        assert newController.getNetworkBlocks().equals(distinctNetwork);
      }
    }
    assert blockDestroyedWasController == originalController.getNetworkBlocks().isEmpty();
  }

  // Record the fiber tile and call once broken. Similar to breakBlock except called on both server and client and the tile is passed in.
//  @Override
//  public boolean removedByPlayer(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, boolean willHarvest) {
//    TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
//    boolean res = super.removedByPlayer(state, worldIn, pos, player, willHarvest);
//    breakFiber(worldIn, pos, tile);
//    return res;
//  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
    breakFiber(worldIn, pos, tile);
    super.breakBlock(worldIn, pos, state);
  }

  /**
   * When a fiber is broken, changes the controller and removes/splits connections if necessary.
   * @param worldIn the world.
   * @param pos the pos of the fiber.
   * @param tile the tile of the broken fiber.
   */
  private void breakFiber(World worldIn, BlockPos pos, TileOpticalFiberBase tile) {
    if (!getConnectedFibers(worldIn, pos).isEmpty()) {
      TileOpticalFiberController controller = tile.getController();
      controller.removeFiber(tile);
      formNewNetworks(worldIn, pos, controller);
    }
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

    if (ModItems.terminator == playerIn.getHeldItem(hand).getItem()) {
      if (!worldIn.isRemote) {
        if (playerIn.isSneaking()) {
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenSideChooser(pos), (EntityPlayerMP) playerIn);
        } else {
          System.out.println("send packet open connections");
          TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(pos, facing, tile.getConnections(facing)), (EntityPlayerMP) playerIn);
        }
      }
      return true;
    }

    return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
  }


}
