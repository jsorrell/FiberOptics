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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransferTypeFluids extends TransferType<IFluidHandler> {
  private static final Item ICON_ITEM = Items.WATER_BUCKET;

  @Override
  public void registerConnections() {
    this.registerConnection(FluidInput.class, new ResourceLocation(FiberOptics.MODID, "input"));
    this.registerConnection(FluidOutput.class, new ResourceLocation(FiberOptics.MODID, "output"));
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
  public String getUnlocalizedName() {
    return "forge_fluids";
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

  public static class FluidInput extends OpticalFiberConnection {
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
    public TransferType getTransferType() {
      return ModTransferTypes.fluidType;
    }
  }

  public static class FluidOutput extends OpticalFiberConnection {
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
    public TransferType getTransferType() {
      return ModTransferTypes.fluidType;
    }
  }
}
