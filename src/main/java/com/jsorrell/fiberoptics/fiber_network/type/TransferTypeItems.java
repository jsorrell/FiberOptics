package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeItems extends TransferType<IItemHandler> {
  private static final Item ICON_ITEM = Items.APPLE;

  @Override
  public void registerConnections() {
    this.registerConnection(ItemInput.class, new ResourceLocation(FiberOptics.MODID, "input"));
    this.registerConnection(ItemOutput.class, new ResourceLocation(FiberOptics.MODID, "output"));
  }

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
  public void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    RenderItem renderItem = mc.getRenderItem();
    renderItem.zLevel = zLevel;
    renderItem.renderItemIntoGUI(new ItemStack(ICON_ITEM), 0, 0);
  }

  @Override
  public void displayCreateConnectionGui(Minecraft mc, BlockPos pos, EnumFacing side) {
    //TODO implement
  }

  @Override
  public void displayEditConnectionGui(Minecraft mc, OpticalFiberConnection connection) {
    //TODO implement
  }

  // TODO delete this
  public static ItemInput createTestInput(BlockPos pos, EnumFacing side, String channelName) {
    return new ItemInput(pos, side, channelName);
  }

  public static class ItemInput extends OpticalFiberConnection {
    private ItemInput(BlockPos pos, EnumFacing side, String channelName) {
      super(pos, side, channelName);
    }

    @SuppressWarnings("unused")
    public ItemInput(ByteBuf buf) {
      super(buf);
    }

    @SuppressWarnings("unused")
    public ItemInput(NBTTagCompound compound) {
      super(compound);
    }

    @Override
    public TransferType getTransferType() {
      return ModTransferTypes.itemType;
    }
  }

  public static class ItemOutput extends OpticalFiberConnection {
    private int priority;

    private ItemOutput(BlockPos pos, EnumFacing side, String channelName, int priority) {
      super(pos, side, channelName);
      this.priority = priority;
    }

    public ItemOutput(ByteBuf buf) {
      super(buf);
      this.priority = buf.readInt();
    }

    public ItemOutput(NBTTagCompound compound) {
      super(compound);
      this.priority = compound.getInteger("priority");
    }

    @Override
    public TransferType getTransferType() {
      return ModTransferTypes.itemType;
    }

    @Override
    public void toBytes(ByteBuf buf) {
      super.toBytes(buf);
      buf.writeInt(this.priority);
    }

    @Nullable
    @Override
    protected NBTTagCompound serializeConnectionSpecificNBT() {
      NBTTagCompound compound = new NBTTagCompound();
      compound.setInteger("priority", this.priority);
      return compound;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ItemOutput)) return false;
      if (!super.equals(o)) return false;
      ItemOutput that = (ItemOutput) o;
      return priority == that.priority;
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), priority);
    }

    @Override
    public int compareTo(OpticalFiberConnection connection) {
      int cmp;
      if ((cmp = super.compareTo(connection)) != 0) return cmp;
      ItemOutput con = (ItemOutput) connection;
      if ((cmp = Integer.compare(this.priority, con.priority)) != 0) return cmp;
      return 0;
    }
  }
}