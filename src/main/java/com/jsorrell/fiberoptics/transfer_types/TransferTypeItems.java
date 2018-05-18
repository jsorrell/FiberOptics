package com.jsorrell.fiberoptics.transfer_types;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Type;

public class TransferTypeItems extends TransferType<IItemHandler> {
  @Override
  public Type getTransferObjectType() {
    return ItemStack.class;
  }

  @Override
  public String toString() {
    return "items";
  }

  @Override
  public Capability<IItemHandler> getCapability() {
    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(IItemHandler itemHandler) {
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      if (!itemHandler.getStackInSlot(i).isEmpty())
        return true;
    }
    return false;
  }
}