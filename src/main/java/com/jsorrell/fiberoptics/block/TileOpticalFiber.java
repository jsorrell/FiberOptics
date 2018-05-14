package com.jsorrell.fiberoptics.block;

public class TileOpticalFiber extends TileOpticalFiberBase {

  private TileOpticalFiberController controllerTile;

  @Override
  public TileOpticalFiberController getControllerTile() {
    return this.controllerTile;
  }

}