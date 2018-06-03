package com.jsorrell.fiberoptics.block.optical_fiber;

import net.minecraft.util.EnumFacing;

import java.util.Optional;

public enum FiberPart {
  DOWN(0),
  UP(1),
  NORTH(2),
  SOUTH(3),
  WEST(4),
  EAST(5),
  CENTER(6);

  public static final FiberPart[] VALUES = { DOWN, UP, NORTH, SOUTH, WEST, EAST, CENTER };
  private final int index;

  FiberPart(int i) {
    this.index = i;
  }

  public static FiberPart fromSide(EnumFacing side) {
    switch (side) {
      case DOWN: return DOWN;
      case UP: return UP;
      case NORTH: return NORTH;
      case SOUTH: return SOUTH;
      case WEST: return WEST;
      case EAST: return EAST;
    }
    throw new AssertionError("Should never get here.");
  }

  public Optional<EnumFacing> getSide() {
    switch (this) {
      case CENTER: return Optional.empty();
      case DOWN: return Optional.of(EnumFacing.DOWN);
      case UP: return Optional.of(EnumFacing.UP);
      case NORTH: return Optional.of(EnumFacing.NORTH);
      case SOUTH: return Optional.of(EnumFacing.SOUTH);
      case WEST: return Optional.of(EnumFacing.WEST);
      case EAST: return Optional.of(EnumFacing.EAST);
    }
    throw new AssertionError("Should never get here.");
  }

  public boolean isSide() {
    return this != CENTER;
  }

  public int getIndex() {
    return this.index;
  }

  public static FiberPart fromIndex(int i) {
    if (i < 0 || VALUES.length <= i) {
      throw new IndexOutOfBoundsException("Index for part is invalid.");
    }
    return VALUES[i];
  }
}
