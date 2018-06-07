package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;

import java.util.Collection;

public class GuiSideListElement extends GuiListElement {
  protected static final int CREATE_BUTTON_X = 137;
  protected static final int CREATE_BUTTON_Y = 3;

  protected static final int SIDE_NAME_X = 6;
  protected static final float SIDE_NAME_SCALE_FACTOR = 1.5F;
  protected static final int SIDE_NAME_COLOR = 0x404040;

  public final EnumFacing side;
  public GuiButtonConnectionAdd addButton = null;

  public GuiSideListElement(int id, EnumFacing side) {
    super(id);
    this.side = side;
  }

  @Override
  public Collection<GuiButton> initListElement(int x, int y) {
    return ImmutableList.of(addButton = new GuiButtonConnectionAdd(id, x + CREATE_BUTTON_X, y + CREATE_BUTTON_Y, this));
  }

  @Override
  public void drawListElement(Minecraft mc, int mouseX, int mouseY, int x, int y, float partialTicks) {
    /* Background */
    super.drawListElement(mc, mouseX, mouseY, x, y, partialTicks);
    /* Side Name */
    String sideName = I18n.format("enumFacing." + this.side.getName() + ".name");
    GlStateManager.pushMatrix();
    GlStateManager.scale(SIDE_NAME_SCALE_FACTOR, SIDE_NAME_SCALE_FACTOR, 1F);
    int sideNameX = (int)((x + SIDE_NAME_X)/SIDE_NAME_SCALE_FACTOR);
    int sideNameY = (int)((y + (TEXTURE.size.height - mc.fontRenderer.FONT_HEIGHT)/2)/ SIDE_NAME_SCALE_FACTOR);
    mc.fontRenderer.drawString(sideName, sideNameX, sideNameY, SIDE_NAME_COLOR);
    GlStateManager.popMatrix();
    /* Add Button */
    if (addButton != null) {
      GlStateManager.color(1F, 1F, 1F , 1F);
      addButton.drawButton(mc, mouseX, mouseY, partialTicks);
    }
  }
}
