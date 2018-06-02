package com.jsorrell.fiberoptics.block.optical_fiber;

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
      this.controller = TileOpticalFiberController.getTileEntity(this.world, this.controllerPos);
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
    this.controller = TileOpticalFiberController.getTileEntity(this.world, pos);
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

  /**
   * Gets the tile entity of type {@link TileOpticalFiber} from the world.
   * Only call this when sure that the tile exists and is of this type.
   * @param world the world.
   * @param pos the position of the tile.
   * @return the tile entity of type {@link TileOpticalFiber}.
   */
  public static TileOpticalFiber getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    Objects.requireNonNull(testTile, "Tile Entity at " + pos + " does not exist");
    if (!(testTile instanceof TileOpticalFiber)) {
      throw new ClassCastException("Tile at " + pos + "  is not instance of TileOpticalFiber");
    }
    return (TileOpticalFiber) testTile;
  }
}