package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.utils.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileOpticalFiber extends TileOpticalFiberBase {

  private BlockPos controllerPos = null;
  private TileOpticalFiberController controller = null;

  // Needed by Forge
  @SuppressWarnings({"unused", "WeakerAccess"})
  public TileOpticalFiber() {
    super();
  }

  @Override
  public BlockPos getControllerPos() {
    return this.controllerPos;
  }

  @Override
  public TileOpticalFiberController getController() {
    if (this.controllerPos == null) {
      throw new NullPointerException("Controller stored in fiber at " + this.pos + " is null.");
    }
    if (this.controller == null) {
      this.controller = Util.getTileChecked(this.world, this.controllerPos, TileOpticalFiberController.class);
    }
    return this.controller;
  }

  /**
   * Stores the location of the controller.
   * @param pos the controller location.
   */
  void setControllerPos(BlockPos pos) {
    Objects.requireNonNull(pos, "Setting controller to null");
    this.controllerPos = pos.toImmutable();
    this.controller = Util.getTileChecked(this.world, pos, TileOpticalFiberController.class);
    this.markDirty();
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    if (this.controllerPos != null) {
      NBTTagCompound controllerPos = new NBTTagCompound();
      controllerPos.setInteger("x", this.controllerPos.getX());
      controllerPos.setInteger("y", this.controllerPos.getY());
      controllerPos.setInteger("z", this.controllerPos.getZ());
      compound.setTag("controller", controllerPos);
    }
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    if (compound.hasKey("controller")) {
      NBTTagCompound controllerPos = compound.getCompoundTag("controller");
      int controllerX = controllerPos.getInteger("x");
      int controllerY = controllerPos.getInteger("y");
      int controllerZ = controllerPos.getInteger("z");
      this.controllerPos = new BlockPos(controllerX, controllerY, controllerZ).toImmutable();
    }
  }
}