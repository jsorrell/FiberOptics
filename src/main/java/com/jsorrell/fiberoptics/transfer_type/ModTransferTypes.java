package com.jsorrell.fiberoptics.transfer_type;

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
    return -1;
  }

  public static TransferType fromIndex(int i) {
    if (0 <= i && i < VALUES.length) {
      return VALUES[i];
    }
    return null;
  }

  public static TransferType fromUnlocalizedName(String unlocalizedName) {
    for (TransferType value : VALUES) {
      if (value.getUnlocalizedName().equals(unlocalizedName)) {
        return value;
      }
    }
    return null;
  }
}