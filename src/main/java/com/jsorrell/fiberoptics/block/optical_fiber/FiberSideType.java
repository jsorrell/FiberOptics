package com.jsorrell.fiberoptics.block.optical_fiber;

public enum FiberSideType {
  NONE(0, "none"),
  SELF_ATTACHMENT(1, "self_attachment"),
  CONNECTION(2, "connection");

  private final int index;
  private final String name;

  FiberSideType(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public static FiberSideType fromIndex(int index) {
    for (FiberSideType fst : FiberSideType.values()) {
      if (fst.index == index) return fst;
    }
    throw new IndexOutOfBoundsException("Index invalid.");
  }

  public static FiberSideType fromName(String name) {
    for (FiberSideType fst : FiberSideType.values()) {
      if (fst.name.equals(name)) return fst;
    }
    throw new IllegalArgumentException("Name \"" + name + "\" invalid.");
  }

  @Override
  public String toString() {
    return getName();
  }
}
