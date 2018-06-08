package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;

public class GuiScreenTypeChooser extends GuiOpticalFiber {

  private static final Point2d[][] BUTTON_POSITIONS = {
          {new Point2d(1/2D, 1/2D)}, // 1
          {new Point2d(1/3D, 1/2D), new Point2d(1D/3D, 1D/2D)}, //2
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D)}, //3
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D), new Point2d(1/2D, 1/2D)}, //4
          {new Point2d(1/4D, 1/4D), new Point2d(3/4D, 1/4D), new Point2d(1/2D, 1/2D), new Point2d(1/4D, 3/4D), new Point2d(3/4D, 3/4D)}, //5
  };

  private static final int[] BUTTON_SIZES = { 96, 40, 32, 32, 28 };

  private final ImmutableList<TransferType> types;
  protected final EnumFacing side;

  public GuiScreenTypeChooser(BlockPos pos, EnumFacing side, Collection<TransferType> types) {
    super(pos);
    this.side = side;
    this.types = ImmutableList.copyOf(types);
  }

  @Override
  public void initGui() {
    super.initGui();
    int size = BUTTON_SIZES[this.types.size()-1];
    for (int i = 0; i < this.types.size(); ++i) {
      Point2d pos = BUTTON_POSITIONS[this.types.size()-1][i];
      int x = this.backgroundStart.x + (int)Math.round(pos.x * EMPTY_BACKGROUND.size.width - size/2D);
      int y = this.backgroundStart.y + (int)Math.round(pos.y * EMPTY_BACKGROUND.size.height - size/2D);
      GuiTypeButton button = new GuiTypeButton(i, x, y, new Dimension(size, size), this.types.get(i));
      this.buttonList.add(button);
    }
  }

  @Override
  public void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    super.drawScreenUnchecked(mouseX, mouseY, partialTicks);
    for (GuiButton button : this.buttonList) {
      if (button instanceof GuiTypeButton) {
        ((GuiTypeButton) button).drawTooltip(this.mc, mouseX, mouseY, partialTicks);
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (0 <= button.id && button.id < this.types.size()) {
      TransferType type = this.types.get(button.id);
      type.displayCreateConnectionGui(this.mc, this.pos, this.side);
    }
    super.actionPerformed(button);
  }

  public static class GuiTypeButton extends GuiButton {
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
}
