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
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeEnergy extends TransferType<IEnergyStorage> {
  private static final SizedTexturePart ICON_TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(0, 43), new Dimension(16, 16));

  public TransferTypeEnergy(ResourceLocation registryKey) {
    super(registryKey);
  }

  @Override
  public void registerConnections() {
    this.registerConnection(new EnergyInputType(new ResourceLocation(FiberOptics.MODID, "input")));
    this.registerConnection(new EnergyOutputType(new ResourceLocation(FiberOptics.MODID, "output")));
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
  public String getLocalizedName() {
    return "Forge Energy";
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    ICON_TEXTURE.drawTexturePart(mc, 0, 0, zLevel);
  }

  /* Energy Input */

  /* Type */
  public class EnergyInputType extends OpticalFiberConnectionType {
    public EnergyInputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeEnergy.this);
    }

    @Override
    public String getShortLocalizedName() {
      return "Input";
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {
    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      onSubmit.accept(new EnergyInput(pos, side, channel));
      return null;
    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new EnergyInput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new EnergyInput(compound);
    }

    /* Instance */
    public class EnergyInput extends OpticalFiberConnection {
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
      public OpticalFiberConnectionType getConnectionType() {
        return EnergyInputType.this;
      }
    }
  }

  /* Output */
  public class EnergyOutputType extends OpticalFiberConnectionType {
    public EnergyOutputType(ResourceLocation registryKey) {
      super(registryKey, TransferTypeEnergy.this);
    }

    @Override
    public String getShortLocalizedName() {
      //FIXME
      return "Output";
    }

    @Override
    public void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks) {

    }

    @Nullable
    @Override
    public GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
      onSubmit.accept(new EnergyOutput(pos, side, channel));
      return null;
    }

    @Override
    public OpticalFiberConnection fromBuf(ByteBuf buf) {
      return new EnergyOutput(buf);
    }

    @Override
    public OpticalFiberConnection fromNBT(NBTTagCompound compound) {
      return new EnergyOutput(compound);
    }

    /* Instance */
    public class EnergyOutput extends OpticalFiberConnection {
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
      public OpticalFiberConnectionType getConnectionType() {
        return EnergyOutputType.this;
      }
    }
  }
}