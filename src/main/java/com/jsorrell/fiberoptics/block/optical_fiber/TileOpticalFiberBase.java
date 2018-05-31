package com.jsorrell.fiberoptics.block.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TileOpticalFiberBase extends TileEntity implements IConnectionContainer {
  private Set<OpticalFiberConnection> connections = null;

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

  private void setConnectedSide(EnumFacing side) {
    // Update blockstate and send to clients
    IBlockState state = this.world.getBlockState(this.pos);
    PropertyBool property = BlockOpticalFiber.getPropertyFromSide(side);
    if (!state.getValue(property)) {
      state = state.withProperty(property, true);
      this.world.setBlockState(this.pos, state, 2);
    }
  }

  @Override
  public boolean addConnection(OpticalFiberConnection connection) {
    if (!isValidConnectionForNetwork(connection)) return false;
    this.getController().fiberNetwork.addConnection(connection);
    this.getController().markDirty();

    TileOpticalFiberBase connectionTile = getTileAtPos(pos);
    if (connectionTile.connections == null) connectionTile.connections = new HashSet<>();
    connectionTile.connections.add(connection);
    connectionTile.markDirty();

    this.setConnectedSide(connection.connectedSide);

    return true;
  }

  @Override
  public boolean removeConnection(OpticalFiberConnection connection) {
    if (this.getTileAtPos(connection.pos).connections.remove(connection)) {
      this.getController().fiberNetwork.removeConnection(connection);
      this.markDirty();

      // Update blockstate and send to clients
      if (BlockOpticalFiber.isFiberInPos(this.world, this.pos.offset(connection.connectedSide))) {
        // TODO do we need this, should we allow this to happen
        return true;
      }

      for (OpticalFiberConnection connection1 : this.connections) {
        if (connection.connectedSide.equals(connection1.connectedSide)) {
          // Another connection exists in this direction so don't update state
          return true;
        }
      }

      PropertyBool property = BlockOpticalFiber.getPropertyFromSide(connection.connectedSide);
      IBlockState state = this.world.getBlockState(this.pos).withProperty(property, false);
      this.world.setBlockState(this.pos, state, 2);
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
      return ImmutableList.copyOf(this.connections.stream().filter(connection -> side.equals(connection.connectedSide)).collect(Collectors.toList()));
    }
  }

  @Override
  public boolean hasConnectionOnSide(EnumFacing side) {
    if (this.connections == null) return false;
    return this.connections.stream().anyMatch(connection -> connection.connectedSide.equals(side));
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
      tileAtPos = getTileEntity(this.world, pos);
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
    oldController.networkBlocks.forEach(pos -> addFiber(TileOpticalFiber.getTileEntity(this.world, pos)));
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

  private NBTTagCompound writeConnectionsToNBT(NBTTagCompound compound) {
    NBTTagList inputs = new NBTTagList();
    NBTTagList outputs = new NBTTagList();
    if (this.connections != null) {
      for (OpticalFiberConnection connection : this.connections) {
        if (connection instanceof OpticalFiberInput) inputs.appendTag(connection.serializeNBT());
        else if (connection instanceof OpticalFiberOutput) outputs.appendTag(connection.serializeNBT());
      }
    }
    compound.setTag("inputs", inputs);
    compound.setTag("outputs", outputs);
    return compound;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    this.writeConnectionsToNBT(compound);
    return super.writeToNBT(compound);
  }

  // TODO find a better way of separating inputs and outputs and possibly others
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound); // Read this.pos

    NBTTagList inputs = compound.getTagList("inputs", Constants.NBT.TAG_COMPOUND);
    NBTTagList outputs = compound.getTagList("outputs", Constants.NBT.TAG_COMPOUND);
    int numInputs = inputs.tagCount();
    int numOutputs = outputs.tagCount();

    if (numInputs > 0 || numOutputs > 0) this.connections = new HashSet<>();


    for (int i = 0; i < numInputs; ++i) {
      OpticalFiberInput input = new OpticalFiberInput(inputs.getCompoundTagAt(i));
      this.connections.add(input);
    }

    for (int i = 0; i < numOutputs; ++i) {
      OpticalFiberOutput output = new OpticalFiberOutput(outputs.getCompoundTagAt(i));
      this.connections.add(output);
    }
  }

  /**
   * Gets the tile entity of type {@link TileOpticalFiberBase} from the world.
   * Only call this when sure that the tile exists and is of this type.
   * @param world the world.
   * @param pos the position of the tile.
   * @return the tile entity of type {@link TileOpticalFiberBase}.
   */
  public static TileOpticalFiberBase getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    Objects.requireNonNull(testTile, "Tile Entity at " + pos + " does not exist");
    if (!(testTile instanceof TileOpticalFiberBase)) {
      throw new ClassCastException("Tile at " + pos + "  is not instance of TileOpticalFiberBase");
    }
    return (TileOpticalFiberBase) testTile;
  }
}
