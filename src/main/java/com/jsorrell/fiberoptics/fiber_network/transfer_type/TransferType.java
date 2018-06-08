package com.jsorrell.fiberoptics.fiber_network.transfer_type;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;

//TODO improve this api

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TransferType<T> {
  public static final Dimension RENDER_SIZE = new Dimension(16, 16);

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
  public final void drawTypeIcon(Minecraft mc, int x, int y, float zLevel, float partialTicks) {
    drawTypeIcon(mc, x, y, zLevel, RENDER_SIZE, partialTicks);
  }

  @SideOnly(Side.CLIENT)
  public final void drawTypeIcon(Minecraft mc, int x, int y, float zLevel, Dimension size, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale((double)size.width / (double)RENDER_SIZE.width, (double)size.height / (double)RENDER_SIZE.height, 1);
    drawTypeIcon(mc, zLevel, partialTicks);
    GlStateManager.popMatrix();
  }

  /**
   * Render a 16x16 icon at (0,0).
   */
  @SideOnly(Side.CLIENT)
  public abstract void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks);
}