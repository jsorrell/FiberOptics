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

  private Boolean isFiberInPos(IBlockAccess worldIn, BlockPos pos) {
    return worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiber;
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

  Set<BlockPos> getConnectedFibers(IBlockAccess worldIn, BlockPos pos) {
    Set<BlockPos> res = new HashSet<>(6);
    for (EnumFacing direction : EnumFacing.VALUES) {
      BlockPos testPos = pos.offset(direction);
      if (isFiberInPos(worldIn, testPos)) {
        res.add(testPos);
      }
    }
    return res;
  }


  public Set<BlockPos> getNetworkedFibers(IBlockAccess worldIn, BlockPos pos) {
    //TODO parallelize
    Set<BlockPos> complete = Collections.synchronizedSet(new HashSet<>());
    ConcurrentLinkedQueue<BlockPos> frontier = new ConcurrentLinkedQueue<>();

    frontier.offer(pos);

    while (!frontier.isEmpty()) {
      BlockPos newFiber = frontier.poll();
      complete.add(newFiber);
      Set<BlockPos> neighbors = getConnectedFibers(worldIn, newFiber);
      neighbors.removeAll(complete);
      frontier.addAll(neighbors);
    }

    return complete;
  }

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
      Set<BlockPos> neighbors = getConnectedFibers(worldIn, newFiber);
      neighbors.removeAll(complete);
      frontier.addAll(neighbors);
    }
    return false;
  }

  public BlockPos findFarthestNetworked(IBlockAccess worldIn, BlockPos pos) {
    Set<BlockPos> networked = getNetworkedFibers(worldIn, pos);
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

  // TODO transfer data from any old controllers
  private void designateController(World worldIn, BlockPos controllerPos) {
    Set<BlockPos> networked = getNetworkedFibers(worldIn, controllerPos);
    networked.remove(controllerPos);

    for (BlockPos fiber : networked) {
      TileEntity testTile = worldIn.getTileEntity(fiber);
      assert testTile instanceof TileOpticalFiber;
      TileOpticalFiber tile = (TileOpticalFiber)testTile;
      tile.setControllerPos(controllerPos);
    }

    worldIn.removeTileEntity(controllerPos);
    TileOpticalFiberController controllerTile = new TileOpticalFiberController();

    worldIn.setTileEntity(controllerPos, controllerTile);
  }

  @Override
  public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
    return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(isController, getConnectedFibers(world, pos).isEmpty());
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(World world, IBlockState state) {
    if (state.getValue(isController)) {
      return new TileOpticalFiberController();
    } else {
      return new TileOpticalFiber();
    }
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    if (state.getValue(isController)) {
      return;
    } else {
      TileOpticalFiber tile = (TileOpticalFiber) worldIn.getTileEntity(pos);

      // Check state of network
      Set<BlockPos> connectedControllers = new HashSet<>();
      for (EnumFacing direction : EnumFacing.VALUES) {
        if (isFiberInDirection(worldIn, pos, direction)) {
          BlockPos controllerPos = ((TileOpticalFiberBase) worldIn.getTileEntity(pos.offset(direction))).getControllerPos();
          if (controllerPos != null) {
            connectedControllers.add(controllerPos);
          }
        }
      }

      if (connectedControllers.isEmpty()) {
        FiberOptics.LOGGER.log(Level.SEVERE, "Fiber at " + pos + " connected to optical fiber but no controller stored in neighbors.");
      } else if (connectedControllers.size() == 1) {
        // If we are connected to a single controller, just store this controller
        tile.setControllerPos(connectedControllers.iterator().next());
      } else {
        // If connected to more than one controller, consolidate them
        Iterator<BlockPos> connectedControllersIterator = connectedControllers.iterator();
        BlockPos primaryControllerPos = connectedControllersIterator.next();
        connectedControllersIterator.remove();
        TileOpticalFiberController primaryController = (TileOpticalFiberController) worldIn.getTileEntity(primaryControllerPos);

        for (BlockPos secondaryControllerPos : connectedControllers) {
          TileOpticalFiberController secondaryController = (TileOpticalFiberController) worldIn.getTileEntity(secondaryControllerPos);
          TileOpticalFiber bridge = new TileOpticalFiber();
          bridge.setControllerPos(primaryControllerPos);
          worldIn.setTileEntity(pos, bridge);
          try {
            primaryController.cannibalizeController(secondaryController);
          } catch (Exception e) {
            // on error, disallow block placement
            FiberOptics.LOGGER.log(Level.SEVERE, "Error joining network: " + e);
            worldIn.removeTileEntity(pos);
            worldIn.setBlockToAir(pos);
          }
        }
      }
    }
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
  }

  // TODO transfer data from any old controllers
  private void fixControllersAfterBreak(World worldIn, Set<BlockPos> controllerPositions, Set<BlockPos> connectedFibers) {
    for (BlockPos connectedFiber : connectedFibers) {
      boolean connectedToController = false;
      for (BlockPos controllerPos : controllerPositions) {
        if(isNetworkedTo(worldIn, controllerPos, connectedFiber)) {
          connectedToController = true;
          break;
        }
      }
      if (!connectedToController) {
        BlockPos farthest = findFarthestNetworked(worldIn, connectedFiber);
        designateController(worldIn, farthest);
        controllerPositions.add(connectedFiber);
      }
    }
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tile = worldIn.getTileEntity(pos);
    if (!(tile instanceof TileOpticalFiberBase)) {
      FiberOptics.LOGGER.log(Level.SEVERE, "Invalid tile entity on broken fiber.");
    } else {

      Set<BlockPos> connectedFibers = getConnectedFibers(worldIn, pos);
      BlockPos controllerPos = ((TileOpticalFiberBase) tile).getControllerPos();

      if (connectedFibers.isEmpty()) {
        if (!((TileOpticalFiberBase) tile).isController()) {
          FiberOptics.LOGGER.log(Level.SEVERE, "Lone fiber broken not controller.");
        }
      } else if (connectedFibers.size() == 1) {
        if (((TileOpticalFiberBase) tile).isController()) {
          BlockPos farthest = findFarthestNetworked(worldIn, connectedFibers.iterator().next());
          designateController(worldIn, farthest);
        } else {
          // Notify controller was destroyed
        }
      } else {
        Set<BlockPos> controllerPositions = new HashSet<>(6);
        if (!((TileOpticalFiberBase) tile).isController()) {
          controllerPositions.add(((TileOpticalFiberBase) tile).getControllerPos());
          // Notify controller was destroyed
        }
        fixControllersAfterBreak(worldIn, controllerPositions, connectedFibers);
      }
    }
    super.breakBlock(worldIn, pos, state);
  }
}
