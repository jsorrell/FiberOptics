package com.jsorrell.fiberoptics.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TileOpticalFiber extends TileOpticalFiberBase {

  private BlockPos controllerPos;

  @Override
  public BlockPos getControllerPos() {
    return controllerPos;
  }

  @Override
  public boolean isController() {
    return false;
  }

  public void setControllerPos(BlockPos pos) {
    this.controllerPos = pos;
    this.markDirty();
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

}