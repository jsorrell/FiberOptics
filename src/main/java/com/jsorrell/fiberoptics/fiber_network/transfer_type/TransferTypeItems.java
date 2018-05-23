package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TransferTypeItems extends TransferType<IItemHandler> {
  @Override
  public Capability<IItemHandler> getCapability() {
    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(@Nonnull IItemHandler input) {
    for (int i = 0; i < input.getSlots(); i++) {
      if (!input.getStackInSlot(i).isEmpty())
        return true;
    }
    return false;
  }

  @Override
  public boolean doTransfer(@Nonnull IItemHandler input, @Nonnull IItemHandler output) {
    for (int i = 0; i < input.getSlots(); i++) {
      ItemStack inputStack = input.extractItem(i, input.getSlotLimit(i), true);
      if (!inputStack.isEmpty()) {
        for (int j = 0; j < output.getSlots(); j++) {
          ItemStack remaining = output.insertItem(j, inputStack, false);
          int numTransferred = inputStack.getCount() - remaining.getCount();
          if (numTransferred > 0) {
            input.extractItem(i, numTransferred, false);
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public String getUnlocalizedName() {
    return "items";
  }
}