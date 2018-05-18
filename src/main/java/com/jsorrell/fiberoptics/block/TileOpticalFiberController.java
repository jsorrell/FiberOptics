package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.transfer_types.ModTransferTypes;
import com.jsorrell.fiberoptics.transfer_types.TransferType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TileOpticalFiberController extends TileOpticalFiberBase implements ITickable {
  private final List<OpticalFiberInput> inputConnections = new ArrayList<>();
  private final List[] outputConnections = new List[ModTransferTypes.VALUES.length];
  private int currentTick = 0;

  private List<OpticalFiberInput> getInputConnections() {
    return this.inputConnections;
  }

  @SuppressWarnings("unchecked")
  private List<OpticalFiberOutput> getOutputConnections(TransferType type) {
    // Lazy instantiation to save memory when most will be null
    int index = ModTransferTypes.getIndex(type);
    List<OpticalFiberOutput> ret = outputConnections[index];
    if (ret == null) {
      ret = new ArrayList<>();
      outputConnections[index] = ret;
    }
    return ret;
  }

  public BlockPos getControllerPos() {
    return this.pos;
  }

  @Override
  public boolean isController() {
    return true;
  }

  // Should be adjacent networks
  //TODO migrate data
  // TODO: Maybe store the controller as pointer and change this and consolidate if the number if different pointers gets too big. do the math on bigO
  public void cannibalizeController(TileOpticalFiberController oldController) {
    this.world.removeTileEntity(oldController.getPos());
    TileOpticalFiber newFiberTile = new TileOpticalFiber();
    this.world.setTileEntity(oldController.getPos(), newFiberTile);
    newFiberTile.setControllerPos(this.pos);
    updateControllerPosition(oldController.pos, this.pos);
  }

  private void updateControllerPosition(BlockPos oldController, BlockPos newController) {
    //TODO parallelize
    Set<BlockPos> complete = Collections.synchronizedSet(new HashSet<>());
    ConcurrentLinkedQueue<BlockPos> frontier = new ConcurrentLinkedQueue<>();
    frontier.add(oldController);


    while (!frontier.isEmpty()) {
      BlockPos newFiber = frontier.poll();
      complete.add(newFiber);
      if (newFiber != oldController) {
        if (world.getTileEntity(newFiber) instanceof TileOpticalFiberController) {
        }
        TileOpticalFiber tile = (TileOpticalFiber) world.getTileEntity(newFiber);
        tile.setControllerPos(newController);
      }
      Set<BlockPos> neighbors = ModBlocks.opticalFiber.getConnectedFibers(world, newFiber);
      for (BlockPos neighbor : neighbors) {
        TileOpticalFiberBase testTile = (TileOpticalFiberBase)world.getTileEntity(neighbor);
        if (!testTile.getControllerPos().equals(newController) && !complete.contains(neighbor)) {
          frontier.add(neighbor);
        }
      }
    }
  }

  public boolean addConnection(OpticalFiberConnection connection) {
    if (connection instanceof OpticalFiberInput) {
      List<OpticalFiberInput> inputs = getInputConnections();
      inputs.add((OpticalFiberInput) connection);
    } else if (connection instanceof OpticalFiberOutput) {
      List<OpticalFiberOutput> outputs = getOutputConnections(connection.getTransferType());
      outputs.add((OpticalFiberOutput) connection);
    }

    System.out.println("Add Connection");
    System.out.println(connection.getClass());
    System.out.println(connection.getPos());
    System.out.println(connection.getConnectedSide());
    System.out.println(connection.getTransferDirection());
    System.out.println(connection.getTransferType());
    return true;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
  }

  @Override
  public void update() {
    //TODO parallelize (maybe. possibly slower on small networks)
    for (OpticalFiberInput input : getInputConnections()) {
      if (input.isOffering(world)) {
        List<OpticalFiberOutput> outputs = getOutputConnections(input.getTransferType());
        for (OpticalFiberOutput output : outputs) {
          if (input.doTransfer(world, output)) {
            // only do one transfer per tick for now
            return;
          }
        }
      }
    }
  }
}
