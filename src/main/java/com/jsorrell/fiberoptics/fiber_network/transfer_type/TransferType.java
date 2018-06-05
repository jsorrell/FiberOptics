package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import com.jsorrell.fiberoptics.util.TexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;

//TODO improve this api

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TransferType<T> {
  public abstract Capability<?> getCapability();

  public abstract boolean isOffering(T input);

  public abstract boolean doTransfer(T input, T output);

  public boolean isSource(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public boolean isSink(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public abstract String getUnlocalizedName();

  public String getName() {
    return I18n.format("transfer_type." + getUnlocalizedName() + ".name");
  }

  @Override
  public String toString() {
    return getName();
  }

  @SideOnly(Side.CLIENT)
  public abstract void renderItemToGui(Minecraft mc, Gui gui, int x, int y, float partialTicks);
}