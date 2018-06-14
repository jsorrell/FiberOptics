package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionType;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Dimension;
import scala.reflect.internal.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransferTypeFluids extends TransferType<IFluidHandler> {
  private static final Item ICON_ITEM = Items.WATER_BUCKET;

  public TransferTypeFluids(ResourceLocation registryKey) {
    super(registryKey);
  }

  @Override
  public String getLocalizedName() {
    return "Fluid";
  }

  @Override
  public void registerConnections() {
    this.registerConnection(new FluidInputType(new ResourceLocation(FiberOptics.MODID, "input")));
    this.registerConnection(new FluidOutputType(new ResourceLocation(FiberOptics.MODID, "output")));
  }

  @Override
  public Capability<IFluidHandler> getCapability() {
    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
  }

  @Override
  public boolean isOffering(@Nonnull IFluidHandler input) {
    FluidStack drained = input.drain(1, false);
    return drained != null && drained.amount > 0;
  }

  @Override
  public boolean doTransfer(@Nonnull IFluidHandler input, @Nonnull IFluidHandler output) {
    FluidStack available = input.drain(1000, false);
    if (available == null || available.amount == 0)
      return false;
    int transferred = output.fill(available, true);
    if (transferred > 0) {
      available.amount = transferred;
      input.drain(available, true);
      return true;
    }
    return false;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawTransferTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    RenderItem renderItem = mc.getRenderItem();
    renderItem.zLevel = zLevel;
    renderItem.renderItemIntoGUI(new ItemStack(ICON_ITEM), 0, 0);
  }

  /* Input Type */
  public class FluidInputType extends OpticalFiberConnectionType {
    public FluidInputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeFluids.this);
    }

    @Override
    public String getShortLocalizedName() {
      return "Input";
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
      (new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/type_icons.png"), new TextureOffset(16, 16), new Dimension(16, 16))).drawTexturePart(mc, 0, 0, zLevel);
    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      onSubmit.accept(new FluidInput(pos, side, channel));
      return null;
    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new FluidInput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new FluidInput(compound);
    }

    public class FluidInput extends OpticalFiberConnection {
      private FluidInput(BlockPos pos, EnumFacing side, String channelName) {
        super(pos, side, channelName);
      }

      public FluidInput(ByteBuf buf) {
        super(buf);
      }

      public FluidInput(NBTTagCompound compound) {
        super(compound);
      }

      @Override
      public OpticalFiberConnectionType getConnectionType() {
        return FluidInputType.this;
      }
    }
  }

  public class FluidOutputType extends OpticalFiberConnectionType {
    public FluidOutputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeFluids.this);
    }

    @Override
    public String getShortLocalizedName() {
      return "Output";
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
      (new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/type_icons.png"), new TextureOffset(32, 16), new Dimension(16, 16))).drawTexturePart(mc, 0, 0, zLevel);
    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      onSubmit.accept(new FluidOutput(pos, side, channel));
      return null;
    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new FluidOutput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new FluidOutput(compound);
    }

    public class FluidOutput extends OpticalFiberConnection {
      private FluidOutput(BlockPos pos, EnumFacing side, String channelName) {
        super(pos, side, channelName);
      }

      public FluidOutput(ByteBuf buf) {
        super(buf);
      }

      public FluidOutput(NBTTagCompound compound) {
        super(compound);
      }

      @Override
      public OpticalFiberConnectionType getConnectionType() {
        return FluidOutputType.this;
      }
    }
  }
}
