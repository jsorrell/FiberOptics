package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import javax.vecmath.Point2d;

public abstract class GuiScreenDistributedButton extends GuiOpticalFiber {
  public static final int MAX_TOOLTIP_TEXT_WIDTH = 80;

  private static final Point2d[][] BUTTON_POSITIONS = {
          {new Point2d(1/2D, 1/2D)}, // 1
          {new Point2d(1/3D, 1/2D), new Point2d(1D/3D, 1D/2D)}, //2
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D)}, //3
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D), new Point2d(1/2D, 1/2D)}, //4
          {new Point2d(1/4D, 1/4D), new Point2d(3/4D, 1/4D), new Point2d(1/2D, 1/2D), new Point2d(1/4D, 3/4D), new Point2d(3/4D, 3/4D)}, //5
          //TODO expand this or find an algorithm. If the latter, put a limit on the options.
  };

  private static final int[] BUTTON_SIZES = { 96, 40, 32, 32, 28 };

  private int numButtons;

  public GuiScreenDistributedButton(int numButtons) {
    this.numButtons = numButtons;
  }

  private int getButtonSize() {
    return BUTTON_SIZES[this.numButtons];
  }

  private Point getButtonPos(int i) {
    Point2d pos = BUTTON_POSITIONS[this.numButtons -1][i];
    int x = this.backgroundStart.getX() + (int)Math.round(pos.x * EMPTY_BACKGROUND.size.getWidth() - this.getButtonSize()/2D);
    int y = this.backgroundStart.getY() + (int)Math.round(pos.y * EMPTY_BACKGROUND.size.getHeight() - this.getButtonSize()/2D);
    return new Point(x, y);
  }

  @Override
  public void initGui() {
    super.initGui();
    for (int i = 0; i < numButtons; ++i) {
      Point buttonPos = this.getButtonPos(i);
      GuiButton button = this.createButton(i, buttonPos.getX(), buttonPos.getY(), this.getButtonSize());
      this.buttonList.add(button);
    }
  }

  @Override
  public void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    super.drawScreenUnchecked(mouseX, mouseY, partialTicks);
    for (int i = 0; i < numButtons; ++i) {
      Point buttonPos = getButtonPos(i);
      if ((new Rectangle(buttonPos, new Dimension(this.getButtonSize(), this.getButtonSize()))).contains(mouseX, mouseY)) {
        this.drawTooltip(i, mouseX, mouseY, partialTicks, this.width, this.height, this.mc.fontRenderer);
      }
    }
  }


  /**
   * Get the button at {@code idx}. The button should be square.
   * @param idx the button idx
   * @param x the x position of the button.
   * @param y the y position of the button.
   * @param size the size of the button.
   * @return the button.
   */
  protected abstract GuiButton createButton(int idx, int x, int y, int size);

  protected void drawTooltip(int idx, int mouseX, int mouseY, float partialTicks, int screenWidth, int screenHeight, FontRenderer fontRenderer) { }
}
