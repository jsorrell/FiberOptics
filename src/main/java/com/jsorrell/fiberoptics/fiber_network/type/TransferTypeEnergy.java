package com.jsorrell.fiberoptics.fiber_network.type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Dimension;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeEnergy extends TransferType<IEnergyStorage> {
  private static final SizedTexturePart ICON_TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(0, 43), new Dimension(16,16));

  @Override
  public void registerConnections() {
    this.registerConnection(EnergyInput.class, new ResourceLocation(FiberOptics.MODID, "input"));
    this.registerConnection(EnergyOutput.class, new ResourceLocation(FiberOptics.MODID, "output"));
  }

  @Override
  public Capability<IEnergyStorage> getCapability() {
    return CapabilityEnergy.ENERGY;
  }

  @Override
  public boolean isSource(TileEntity tile, EnumFacing side) {
    IEnergyStorage capabilityHandler;
    if ((capabilityHandler = tile.getCapability(this.getCapability(), side)) != null) {
      return capabilityHandler.canExtract();
    }
    return false;
  }

  @Override
  public boolean isSink(TileEntity tile, EnumFacing side) {
    IEnergyStorage capabilityHandler;
    if ((capabilityHandler = tile.getCapability(this.getCapability(), side)) != null) {
      return capabilityHandler.canReceive();
    }
    return false;
  }

  @Override
  public boolean isOffering(@Nonnull IEnergyStorage input) {
    return input.extractEnergy(1, true) > 0;
  }

  @Override
  public boolean doTransfer(@Nonnull IEnergyStorage input, IEnergyStorage output) {
    int available = input.extractEnergy(1000, true);
    int transferred = output.receiveEnergy(available, false);
    if (transferred > 0) {
      input.extractEnergy(transferred, false);
      return true;
    }
    return false;
  }

  @Override
  public String getUnlocalizedName() {
    return "forge_energy";
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    ICON_TEXTURE.drawTexturePart(mc, 0, 0, zLevel);
  }

  @Override
  public void displayCreateConnectionGui(Minecraft mc, BlockPos pos, EnumFacing side) {
    //TODO implement
  }

  @Override
  public void displayEditConnectionGui(Minecraft mc, OpticalFiberConnection connection) {
    //TODO implement
  }

  public static class EnergyInput extends OpticalFiberConnection {
    public EnergyInput(BlockPos pos, EnumFacing side, String channelName) {
      super(pos, side, channelName);
    }

    public EnergyInput(ByteBuf buf) {
      super(buf);
    }

    public EnergyInput(NBTTagCompound compound) {
      super(compound);
    }

    @Override
    public TransferType getTransferType() {
      return ModTransferTypes.energyType;
    }
  }

  public static class EnergyOutput extends OpticalFiberConnection {
    public EnergyOutput(BlockPos pos, EnumFacing side, String channelName) {
      super(pos, side, channelName);
    }

    public EnergyOutput(ByteBuf buf) {
      super(buf);
    }

    public EnergyOutput(NBTTagCompound compound) {
      super(compound);
    }

    @Override
    public TransferType getTransferType() {
      return ModTransferTypes.energyType;
    }
  }
}