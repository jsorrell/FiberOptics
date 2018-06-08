package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiTypeButton extends GuiButton {
  public static final int MAX_TOOLTIP_TEXT_WIDTH = 80;

  protected final Dimension size;
  public final TransferType type;

  public GuiTypeButton(int id, int x, int y, Dimension size, TransferType type) {
    super(id, x, y, size.width, size.height, "");
    this.type = type;
    this.size = size;
  }

  @Override
  public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (this.visible) {
      GlStateManager.color(1, 1, 1, 1);
      GlStateManager.disableDepth();
      GlStateManager.disableLighting();
      // Icon
      this.type.drawTypeIcon(mc, x, y, this.zLevel, size, partialTicks);
    }
  }

  public void drawTooltip(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (this.visible) {
      // Tooltip
      this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
      if (mc.currentScreen != null && this.getHoverState(this.hovered) == 2) {
        GuiUtils.drawHoveringText(ImmutableList.of(this.type.getName()), mouseX, mouseY, mc.currentScreen.width, mc.currentScreen.height, MAX_TOOLTIP_TEXT_WIDTH, mc.fontRenderer);
      }
    }
  }
}

