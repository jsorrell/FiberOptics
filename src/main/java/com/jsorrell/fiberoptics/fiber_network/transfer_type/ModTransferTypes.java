package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModTransferTypes {
  public static final TransferTypeItems ITEMS = new TransferTypeItems();
  public static final TransferTypeFluids FORGE_FLUIDS = new TransferTypeFluids();
  public static final TransferTypeEnergy FORGE_ENERGY = new TransferTypeEnergy();


  public static final TransferType[] VALUES = {
          ITEMS,
          FORGE_FLUIDS,
          FORGE_ENERGY
  };

  public static int getIndex(TransferType t) {
    for (int i = 0; i < VALUES.length; i++) {
      if (t == VALUES[i]) {
        return i;
      }
    }
    throw new IllegalArgumentException("Invalid transfer type.");
  }

  public static TransferType fromIndex(int i) {
    if (0 <= i && i < VALUES.length) {
      return VALUES[i];
    }
    throw new IndexOutOfBoundsException("Index is not transfer type.");
  }

  public static TransferType fromUnlocalizedName(String unlocalizedName) {
    for (TransferType value : VALUES) {
      if (value.getUnlocalizedName().equals(unlocalizedName)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Invalid tranfer type.");
  }
}