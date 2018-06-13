package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionType;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeItems extends TransferType<IItemHandler> implements Serializable {
  private static final Item ICON_ITEM = Items.APPLE;

  public TransferTypeItems(ResourceLocation registryKey) {
    super(registryKey);
  }

  @Override
  public void registerConnections() {
    this.registerConnection(new ItemInputType(new ResourceLocation(FiberOptics.MODID, "input")));
    this.registerConnection(new ItemOutputType(new ResourceLocation(FiberOptics.MODID, "output")));
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
  public String getLocalizedName() {
    return "Item";
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    RenderItem renderItem = mc.getRenderItem();
    renderItem.zLevel = zLevel;
    renderItem.renderItemIntoGUI(new ItemStack(ICON_ITEM), 0, 0);
  }

  /* Input */
  public class ItemInputType extends OpticalFiberConnectionType {

    public ItemInputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeItems.this);
    }

    @Override
    public String getShortLocalizedName() {
      return "Input";
    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      onSubmit.accept(new ItemInput(pos, side, channel));
      return null;
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {

    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new ItemInput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new ItemInput(compound);
    }

    public class ItemInput extends OpticalFiberConnection {
      private ItemInput(BlockPos pos, EnumFacing side, String channelName) {
        super(pos, side, channelName);
      }

      public ItemInput(ByteBuf buf) {
        super(buf);
      }

      public ItemInput(NBTTagCompound compound) {
        super(compound);
      }

      @Override
      public OpticalFiberConnectionType getConnectionType() {
        return ItemInputType.this;
      }
    }
  }

  /* Output */
  public class ItemOutputType extends OpticalFiberConnectionType {
    public ItemOutputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeItems.this);
    }

    @Override
    public String getShortLocalizedName() {
      return "Output";
    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      //FIXME
      onSubmit.accept(new ItemOutput(pos, side, channel, 0));
      return null;
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {

    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new ItemOutput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new ItemOutput(compound);
    }

    public class ItemOutput extends OpticalFiberConnection {
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
        this.priority = compound.getInteger("Priority");
      }

      @Override
      public OpticalFiberConnectionType getConnectionType() {
        return ItemOutputType.this;
      }

      @Override
      public void writeConnectionSpecificBytes(ByteBuf buf) {
        buf.writeInt(this.priority);
      }

      @Nullable
      @Override
      protected NBTTagCompound serializeConnectionSpecificNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Priority", this.priority);
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
}