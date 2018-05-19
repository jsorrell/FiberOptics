package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.logging.Level;

public class TileOpticalFiber extends TileOpticalFiberBase {

  private BlockPos controllerPos;

  // Needed when loading world before readFromNBT called
  @SuppressWarnings("unused")
  public TileOpticalFiber() {
    this(BlockPos.ORIGIN);
  }

  public TileOpticalFiber(BlockPos controllerPos) {
    this.controllerPos = controllerPos;
  }

  @Override
  public BlockPos getControllerPos() {
    return controllerPos;
  }

  @Override
  public boolean isController() {
    return false;
  }

  boolean setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
    this.markDirty();
    return true;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setInteger("controllerX", controllerPos.getX());
    compound.setInteger("controllerY", controllerPos.getY());
    compound.setInteger("controllerZ", controllerPos.getZ());
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    int controllerX = compound.getInteger("controllerX");
    int controllerY = compound.getInteger("controllerY");
    int controllerZ = compound.getInteger("controllerZ");
    this.controllerPos = new BlockPos(controllerX, controllerY, controllerZ);
    super.readFromNBT(compound);
  }

  public static TileOpticalFiber getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    if (testTile == null) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile Entity at " + pos + " does not exist: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }
    if (!(testTile instanceof TileOpticalFiber)) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile at " + pos + "  is not instance of TileOpticalFiber: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }

    return (TileOpticalFiber) testTile;
  }
}