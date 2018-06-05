package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.TexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeItems extends TransferType<IItemHandler> {
  private static final Item ICON_ITEM = Items.APPLE;

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

  @Override
  @SideOnly(Side.CLIENT)
  public void renderItemToGui(Minecraft mc, Gui gui, int x, int y, float partialTicks) {
    mc.getRenderItem().renderItemIntoGUI(new ItemStack(ICON_ITEM), x, y);
  }
}