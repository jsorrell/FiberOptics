package com.jsorrell.fiberoptics.block.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.util.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TileOpticalFiberBase extends TileEntity implements IConnectionContainer {
  private Set<OpticalFiberConnection> connections = null;
  private final List<EnumFacing> selfAttachments = new ArrayList<>(6);

  /**
   * Gets the position of the controller linked to the fiber.
   * @return the position of the controller linked to the fiber.
   */
  public abstract BlockPos getControllerPos();

  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
    return oldState.getValue(BlockOpticalFiber.isController) != newState.getValue(BlockOpticalFiber.isController);
  }

  /**
   * Gets the controller linked to the fiber.
   * @return the controller linked to the fiber1.
   */
  abstract TileOpticalFiberController getController();

  @Override
  public boolean addConnection(OpticalFiberConnection connection) {
    if (!isValidConnectionForNetwork(connection)) return false;
    this.getController().fiberNetwork.addConnection(connection);
    this.getController().markDirty();

    TileOpticalFiberBase connectionTile = getTileAtPos(pos);
    if (connectionTile.connections == null) connectionTile.connections = new HashSet<>();
    connectionTile.connections.add(connection);
    connectionTile.markDirty();

    BlockOpticalFiber.setSideType(this.world, this.pos, connection.side, FiberSideType.CONNECTION);

    return true;
  }

  @Override
  public boolean replaceConnection(OpticalFiberConnection connection, OpticalFiberConnection connectionToReplace) {

    if (connection.pos != connectionToReplace.pos || connection.side != connectionToReplace.side) {
      return false;
    }

    if (!isValidConnectionForNetwork(connection)) return false;


    TileOpticalFiberBase connectionTile = getTileAtPos(connection.pos);

    if (!connectionTile.connections.remove(connectionToReplace)) return false;
    connectionTile.connections.add(connection);
    connectionTile.markDirty();
    this.getController().fiberNetwork.removeConnection(connectionToReplace);
    this.getController().fiberNetwork.addConnection(connection);
    this.getController().markDirty();
    return true;
  }

  @Override
  public boolean removeConnection(OpticalFiberConnection connection) {
    if (this.getTileAtPos(connection.pos).connections.remove(connection)) {
      this.getController().fiberNetwork.removeConnection(connection);
      this.markDirty();

      for (OpticalFiberConnection connection1 : this.connections) {
        if (connection.side.equals(connection1.side)) {
          // Another connection exists in this direction so don't update state
          return true;
        }
      }

      BlockOpticalFiber.setSideType(this.world, this.pos, connection.side, FiberSideType.NONE);
      return true;
    }
    return false;
  }

  @Override
  public ImmutableList<OpticalFiberConnection> getConnections(@Nullable EnumFacing side) {
    if (this.connections == null) return ImmutableList.of();

    if (side == null) {
      return ImmutableList.copyOf(this.connections);
    } else {
      return ImmutableList.copyOf(this.connections.stream().filter(connection -> side.equals(connection.side)).collect(Collectors.toList()));
    }
  }

  @Override
  public boolean hasConnectionOnSide(EnumFacing side) {
    if (this.connections == null) return false;
    return this.connections.stream().anyMatch(connection -> connection.side.equals(side));
  }

  /**
   * Imports the connections from {@code fiber}.
   * {@code fiber} should not be used after this is called.
   * @param fiber the fiber to import.
   */
  void importConnections(TileOpticalFiberBase fiber) {
    this.connections = fiber.connections;
  }

  /**
   * Gets a {@link TileOpticalFiberBase} at a position.
   * Attemps to do this efficiently by avoiding calls to {@link IBlockAccess#getTileEntity(BlockPos)}
   * @param pos the position.
   * @return the tile at the position.
   */
  private TileOpticalFiberBase getTileAtPos(BlockPos pos) {
    TileOpticalFiberBase tileAtPos;
    if (pos.equals(this.pos)) {
      tileAtPos = this;
    } else if (pos.equals(this.getController().pos)) {
      tileAtPos = this.getController();
    } else {
      tileAtPos = Util.getTileChecked(this.world, pos, TileOpticalFiberBase.class);
    }
    return tileAtPos;
  }

  /**
   * Removes a {@link BlockOpticalFiber} from the network.
   * @param tileRemoved the fiber to remove.
   * @return {@code true} iff the fiber was in the network and was removed.
   */
  boolean removeFiber(TileOpticalFiberBase tileRemoved) {
    boolean successfullyRemoved0 = this.getController().networkBlocks.remove(tileRemoved.getPos());
    Set<OpticalFiberConnection> connectionsRemoved = tileRemoved.connections;
    boolean successfullyRemoved1 = connectionsRemoved == null || this.getController().fiberNetwork.removeAllConnections(connectionsRemoved);
    assert successfullyRemoved0 == successfullyRemoved1;
    if (successfullyRemoved0) this.getController().markDirty();
    return successfullyRemoved0;
  }

  /**
   * Imports all the fibers and connections from a controller.
   * @param oldController the controller to import from.
   */
  void cannibalize(TileOpticalFiberController oldController) {
    oldController.networkBlocks.forEach(pos -> addFiber(Util.getTileChecked(this.world, pos, TileOpticalFiber.class)));
    this.getController().fiberNetwork.cannibalize(oldController.fiberNetwork);
    this.getController().markDirty();
  }

  /**
   * Adds a {@link BlockOpticalFiber} at {@link BlockPos} to the controller.
   * Use this whenever a fiber is added to a controller.
   * @param opticalFiber the fiber to add
   */
  void addFiber(TileOpticalFiber opticalFiber) {
    if (this.getController().networkBlocks.contains(opticalFiber.pos)) {
      return;
    }
    opticalFiber.setControllerPos(this.getControllerPos());
    opticalFiber.markDirty();
    this.getController().fiberNetwork.addAllConnections(((TileOpticalFiberBase)opticalFiber).connections);
    this.getController().networkBlocks.add(opticalFiber.pos);
    this.getController().markDirty();
  }

  /**
   * Migrates fibers from {@code this} to {@code newController}. This is used to split the network.
   * @param newController the controller to migrate to.
   * @param fibersToMigrate the positions of fibers to migrate.
   */
  void migrateFibersTo(TileOpticalFiberController newController, Set<BlockPos> fibersToMigrate) {
    if (!this.getController().networkBlocks.containsAll(fibersToMigrate)) {
      throw new AssertionError("Doesn't contain all fibers to migrate.");
    }

    if (fibersToMigrate.contains(this.getControllerPos())) {
      throw new AssertionError("Contains this controller.");
    }

    for (BlockPos fiberPos : fibersToMigrate) {
      // Move to the new network
      TileOpticalFiberBase fiber = Util.getTileChecked(this.world, fiberPos, TileOpticalFiberBase.class);
      boolean success = this.removeFiber(fiber);
      assert success;
      if (!fiberPos.equals(newController.getPos())) {
        newController.addFiber((TileOpticalFiber) fiber);
      }
    }
  }

  /**
   * Checks if the connection is valid and able to be added to the network.
   * @param connection the connection.
   * @return {@code true} iff the connection is valid and able to be added to the network.
   */
  private boolean isValidConnectionForNetwork(OpticalFiberConnection connection) {
    //TODO fill this out
    return this.getController().networkBlocks.contains(connection.pos);
  }

  /**
   * Gets a set of positions of all {@link BlockOpticalFiber}s in the network.
   * @return the set of all blocks in the network.
   */
  public Set<BlockPos> getNetworkBlocks() {
    // Shallow copy is fine
    @SuppressWarnings("unchecked")
    Set<BlockPos>ret = (Set<BlockPos>)this.getController().networkBlocks.clone();
    return ret;
  }

  public List<String> getChannels() {
    return this.getController().fiberNetwork.getChannels();
  }

  public Collection<TransferType> getAvailableTypes() {
    // TODO implement this
    return TransferType.getRegisteredTypes();
  }

  private NBTTagList serializeConnections() {
    NBTTagList connections = new NBTTagList();
    if (this.connections != null) {
      for (OpticalFiberConnection connection : this.connections) {
        connections.appendTag(connection.serializeNBT());
      }
    }
    return connections;
  }

  private NBTTagList serializeSelfAttachments() {
    NBTTagList selfAttachmentsNBT = new NBTTagList();
    for (EnumFacing side : selfAttachments) {
      selfAttachmentsNBT.appendTag(new NBTTagInt(side.getIndex()));
    }
    return selfAttachmentsNBT;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setTag("Connections", this.serializeConnections());
    compound.setTag("SelfAttachments", this.serializeSelfAttachments());
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound); // Read this.pos

    NBTTagList connectionsNBT = compound.getTagList("Connections", Constants.NBT.TAG_COMPOUND);

    int numConnections = connectionsNBT.tagCount();

    if (numConnections > 0) this.connections = new HashSet<>();

    for (int i = 0; i < numConnections; ++i) {
      OpticalFiberConnection connection;
      try {
        connection = OpticalFiberConnection.fromNBT(connectionsNBT.getCompoundTagAt(i));
      } catch (OpticalFiberConnection.InvalidTypeKeyException | OpticalFiberConnection.InvalidConnectionKeyException e) {
        // Don't restore these connections. Just remove them from the world by not restoring.
        FiberOptics.LOGGER.log(Level.WARNING, e.toString());
        return;
      }
      this.connections.add(connection);
    }

    NBTTagList selfAttachmentsNBT = compound.getTagList("SelfAttachments", Constants.NBT.TAG_INT);
    selfAttachmentsNBT.iterator().forEachRemaining(sideNBT -> this.selfAttachments.add(EnumFacing.getFront(((NBTTagInt) sideNBT).getInt())));
  }

  @Override
  public void onLoad() {
    IBlockState state = this.world.getBlockState(this.pos);
    for (EnumFacing side : this.selfAttachments) {
      state = state.withProperty(BlockOpticalFiber.getPropertyFromSide(side), FiberSideType.SELF_ATTACHMENT);
    }

    for (EnumFacing side : EnumFacing.VALUES) {
      if (this.hasConnectionOnSide(side)) {
        state = state.withProperty(BlockOpticalFiber.getPropertyFromSide(side), FiberSideType.CONNECTION);
      }
    }

    world.setBlockState(this.pos, state, 0);
    super.onLoad();
  }
}
