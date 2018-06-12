package com.jsorrell.fiberoptics.client.gui.components;

import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.util.Dimension;

import javax.annotation.Nonnull;
import java.util.List;

public class GuiIconButton extends GuiButton {
  public static final int MAX_TOOLTIP_TEXT_WIDTH = 80;

  protected final Dimension size;
  protected final SizedTexturePart icon;
  public List<String> tooltip = null;

  public GuiIconButton(int id, int x, int y, Dimension size, SizedTexturePart icon) {
    super(id, x, y, size.getWidth(), size.getHeight(), "");
    this.size = size;
    this.icon = icon;
  }

  @Override
  public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (this.visible) {
      GlStateManager.resetColor();
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0);
      double scale = (double)size.getWidth() / (double)icon.size.getWidth();
      GlStateManager.scale(scale, scale, 1);
      icon.drawTexturePart(mc, 0, 0, this.zLevel);
      GlStateManager.popMatrix();
    }
  }

  public void drawTooltip(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (this.visible && tooltip != null) {
      this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
      if (mc.currentScreen != null && this.getHoverState(this.hovered) == 2) {
        GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, mc.currentScreen.width, mc.currentScreen.height, MAX_TOOLTIP_TEXT_WIDTH, mc.fontRenderer);
      }
    }
  }
}
