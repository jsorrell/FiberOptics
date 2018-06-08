package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TexturePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiScroller extends Gui {
  public final int id;
  protected final int scrollDistance;
  private final SizedTexturePart texture, lockedTexture;

  protected Point scrollStart;
  protected AADirection2D scrollDirection;
  private int scrollPosition;
  private boolean isScrolling;
  private boolean locked = false;

  public GuiScroller(int id, int x, int y, AADirection2D scrollDirection, int scrollDistance, Dimension scrollerSize, TexturePart texture, TexturePart lockedTexture) {
    this.id = id;
    this.scrollStart = new Point(x, y);
    this.scrollDistance = scrollDistance - scrollDirection.axis.getRelevantSize(scrollerSize);
    this.texture = new SizedTexturePart(texture, scrollerSize);
    this.lockedTexture = new SizedTexturePart(lockedTexture, scrollerSize);
    this.scrollDirection = scrollDirection;
  }

  /**
   * Draws the scroller to the screen.
   * @see net.minecraft.client.gui.GuiScreen#drawScreen(int, int, float)
   */
  public void drawScroller(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    GlStateManager.disableDepth();

    SizedTexturePart texture = locked ? this.lockedTexture : this.texture;
    Point scrollerPos = getScrollerPos();
    texture.drawTexturePart(mc, scrollerPos.x, scrollerPos.y, this.zLevel);
    GlStateManager.enableDepth();
  }

  /**
   * Gets the pixel position of the visible part of the scroller.
   * @return the point representing the position of the scroller.
   */
  private Point getScrollerPos() {
    int xPos = this.scrollStart.x + this.scrollDirection.x * scrollPosition;
    int yPos = this.scrollStart.y + this.scrollDirection.y * scrollPosition;
    return new Point(xPos, yPos);
  }

  /**
   * Scrolls the scrollbar to the pixel at {@code pos}.
   * @param pos the pixel to scroll to.
   * @return {@code true} iff the position of the scrollbar changes.
   */
  private boolean scrollTo(int pos) {
    pos = MathHelper.clamp(pos, 0, scrollDistance);

    if (pos != this.scrollPosition) {
      this.scrollPosition = pos;
      return true;
    }
    return false;
  }

  /**
   * Scrolls the scrollbar to {@code pos}.
   * Is clamped.
   * @param pos the position to scroll to.
   * @return {@code true} iff the position of the scrollbar changed.
   */
  public boolean scrollTo(float pos) {
    return this.scrollTo(Math.round(scrollDistance * pos));
  }

  /**
   * Scrolls the scrollbar by {@code distance}.
   * Is clamped.
   * @param distance the amount to move the scrollbar.
   * @return {@code true} iff the position of the scrollbar changed.
   */
  public boolean scroll(float distance) {
    return this.scrollTo(this.getScrollPosition() + distance);
  }

  /**
   * Gets the current scroll position.
   * @return the current scroll position between {@code 0.0F} and {@code 1.0F}.
   */
  public float getScrollPosition() {
    return (float) ((double) this.scrollPosition / (double) this.scrollDistance);
  }

  /**
   * Handles mouse click events.
   * @see net.minecraft.client.gui.GuiScreen#mouseClicked(int, int, int)
   */
  public void mouseClicked(int mouseX, int mouseY, int mouseButton)
  {

    if (!this.locked && mouseButton == 0)
    {
      Point scrollerPos = getScrollerPos();
      Rectangle scrollerBox = new Rectangle(getScrollerPos(), this.texture.size);
      if (scrollerBox.contains(mouseX, mouseY)) {
        this.isScrolling = true;
      }
    }
  }

  /**
   * Handles mouse release events.
   * @see net.minecraft.client.gui.GuiScreen#mouseReleased(int, int, int)
   */
  public void mouseReleased(int mouseX, int mouseY, int state)
  {
    if (this.isScrolling && state == 0)
    {
      this.isScrolling = false;
    }
  }

  public void setLocked(boolean locked) {
    if (locked) {
      this.locked = true;
      this.isScrolling = false;
    } else {
      this.locked = false;
    }
  }

  public boolean isLocked() {
    return this.locked;
  }

  /**
   * Sets the position of scroller.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   */
  public void setPos(int x, int y) {
    this.scrollStart = new Point(x, y);
  }

  /**
   * Updates the mouse position of the scroller.
   * Call this whenever the mouse position changes.
   * @param mouseY the Y coordinate of the mouse.
   * @return {@code true} iff the position of the scrollbar changed.
   */
  public boolean updateMousePosition(int mouseX, int mouseY) {
    if (this.isScrolling) {
      int relevantMousePos = this.scrollDirection.axis.getRelevantCoordinate(mouseX, mouseY);
      int relevantStartPos = this.scrollDirection.axis.getRelevantCoordinate(this.scrollStart);
      return this.scrollTo(scrollDirection.axisDirection() * (relevantMousePos - relevantStartPos));
    }
    return false;
  }

  /**
   * Use this to handle scrollwheel events.
   * @param scrollSpeed the distance to scroll per tick of the mousewheel.
   * @return {@code true} iff the position of the scrollbar changed.
   */
  public boolean handleScrollWheel(float scrollSpeed) {
    if (!this.locked) {
      int scrollDirection = Mouse.getEventDWheel();
      if (scrollDirection != 0) {
        float scrollVal = scrollDirection > 0 ? -scrollSpeed : scrollSpeed;
        return this.scroll(scrollVal);
      }
    }
    return false;
  }
}
