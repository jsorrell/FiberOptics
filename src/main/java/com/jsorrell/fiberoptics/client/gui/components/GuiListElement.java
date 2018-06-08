package com.jsorrell.fiberoptics.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.Collection;

@SideOnly(Side.CLIENT)
public class GuiListElement extends Gui {
  protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new Dimension(157, 23));

  protected final int id;

  public GuiListElement(int id) {
    this.id = id;
  }

  public Collection<GuiButton> initListElement(int x, int y) {
    return ImmutableList.of();
  }

  public void drawListElement(Minecraft mc, int mouseX, int mouseY, int x, int y, float partialTicks) {
    GlStateManager.color(1F, 1F, 1F, 1F);
    TEXTURE.drawTexturePart(mc, x, y, this.zLevel);
  }
}
