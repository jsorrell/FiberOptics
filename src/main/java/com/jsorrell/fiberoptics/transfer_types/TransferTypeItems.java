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
  public boolean isOffering(IItemHandler input) {
    for (int i = 0; i < input.getSlots(); i++) {
      if (!input.getStackInSlot(i).isEmpty())
        return true;
    }
    return false;
  }

  /**
   *
   * @param output
   * @param stack
   * @return The number transferred
   */
  private int doStackTransfer(IItemHandler output, ItemStack stack) {
    for (int i = 0; i < output.getSlots(); i++) {
      ItemStack remainingStack;
      if (!(remainingStack = output.insertItem(i, stack, false)).equals(stack)) {
        return stack.getCount() - remainingStack.getCount();
      }
    }
    return 0;
  }

  @Override
  public boolean doTransfer(IItemHandler input, IItemHandler output) {
    for (int i = 0; i < input.getSlots(); i++) {
      ItemStack inputStack = input.extractItem(i, input.getSlotLimit(i), true);
      if (!inputStack.isEmpty()) {
        int numTransferred = doStackTransfer(output, inputStack);
        if (numTransferred > 0) {
          input.extractItem(i, numTransferred, false);
          return true;
        }
      }
    }
    return false;
  }
}