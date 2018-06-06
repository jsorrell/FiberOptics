package com.jsorrell.fiberoptics.util;

import javax.vecmath.Vector2d;

public enum AADirection2D {
  UP(0, -1, "up", Axis2D.Y),
  RIGHT(1, 0, "right", Axis2D.X),
  DOWN(0, 1, "down", Axis2D.Y),
  LEFT(-1, 0, "left", Axis2D.X);

  public final int x;
  public final int y;
  public final String name;
  public final Axis2D axis;

  AADirection2D(int x, int y, String name, Axis2D axis) {
    this.x = x;
    this.y = y;
    this.name = name;
    this.axis = axis;
  }

  public int axisDirection() {
    return axis == Axis2D.X ? this.x : this.y;
  }
  public final Vector2d getVector() {
    return new Vector2d(x, y);
  }

  @Override
  public String toString() {
    return name;
  }
}
