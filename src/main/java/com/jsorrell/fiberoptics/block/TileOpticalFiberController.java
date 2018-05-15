package com.jsorrell.fiberoptics.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TileOpticalFiberController extends TileOpticalFiberBase {

  public BlockPos getControllerPos() {
    return this.pos;
  }

  @Override
  public boolean isController() {
    return true;
  }

  public void addFiber(TileOpticalFiber newFiber) {

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

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
  }
}
