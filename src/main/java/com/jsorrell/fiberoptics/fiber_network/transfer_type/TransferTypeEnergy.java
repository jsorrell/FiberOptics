package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TransferTypeEnergy extends TransferType<IEnergyStorage> {
  private static final SizedTexturePart ICON_TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(0, 43), new Dimension(16,16));

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
  public void renderItemToGui(Minecraft mc, Gui gui, int x, int y, float partialTicks) {
    ICON_TEXTURE.drawTexturePart(mc, gui, x, y);
  }
}