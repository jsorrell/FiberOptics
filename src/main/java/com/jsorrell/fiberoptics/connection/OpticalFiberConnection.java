package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

abstract class OpticalFiberConnection {
  private BlockPos pos;
  private TileOpticalFiberController controller;
  private TileEntity connectedTile;
  private EnumFacing connectedSide;

  public OpticalFiberConnection(BlockPos pos, TileOpticalFiberController controller, TileEntity connectedTile, EnumFacing connectedSide) {
    this.pos = pos;
    this.controller = controller;
    this.connectedTile = connectedTile;
    this.connectedSide = connectedSide;
  }

  public enum ConnectionType {
    ITEMS,
    FORGE_FLUIDS,
    FORGE_ENERGY
  }

  public enum ConnectionDirection {
    INPUT,
    OUTPUT
  }

  public BlockPos getPos() {
    return pos;
  }

  public TileOpticalFiberController getController() {
    return controller;
  }

  public TileEntity getConnectedTile() {
    return connectedTile;
  }

  public EnumFacing getConnectedSide() {
    return connectedSide;
  }

  public abstract ConnectionType getConnectionType();
  public ConnectionDirection getConnectionDirection() {
    if (this instanceof OpticalFiberInput) {
      return ConnectionDirection.INPUT;
    }
    if (this instanceof OpticalFiberOutput) {
      return ConnectionDirection.OUTPUT;
    }
    return null;
  }
}