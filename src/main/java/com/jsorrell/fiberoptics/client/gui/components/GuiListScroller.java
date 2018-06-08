package com.jsorrell.fiberoptics.client.gui.components;

import com.jsorrell.fiberoptics.client.gui.components.GuiScroller;
import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.TexturePart;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiListScroller extends GuiScroller {
  private int listSize;

  public GuiListScroller(int id, int x, int y, AADirection2D scrollDirection, int scrollHeight, Dimension scrollerSize, TexturePart texture, TexturePart lockedTexture, int listSize) {
    super(id, x, y, scrollDirection, scrollHeight, scrollerSize, texture, lockedTexture);
    this.listSize = listSize;
  }

  /**
   * Sets the list size.
   * @param listSize the new list size.
   * @param keepScrolledIndex if {@code true} will shift the scrollbar such that the currently scrolled position does not change.
   */
  public void setListSize(int listSize, boolean keepScrolledIndex) {
    int preScrollPosition = this.getListScrollPosition();
    this.listSize = listSize;
    if (keepScrolledIndex) {
      this.scrollListTo(preScrollPosition);
    }
  }

  /**
   * Gets the list size.
   * @return the list size.
   */
  public int getListSize() {
    return this.listSize;
  }

  /**
   * Sets the position of the scrollbar to that which corresponds to a certain element of a list.
   * Returns the natural position of (i + i/(n-1))/n so both hand scrolling and scrolling by index look natural and smooth.
   * This is clamped.
   * @param index the position in the list.
   * @return {@code true} iff the index currently scrolled to changed.
   */
  public boolean scrollListTo(int index) {
    float pos;
    if (index <= 0) pos = 0F;
    else if (index >= this.listSize-1) pos = 1F;
    else pos = (float)(((double)index + (double)index/(double)(this.listSize-1)) / (double) this.listSize);

    int oldScrollPositionIndex = this.getListScrollPosition();
    this.scrollTo(pos);
    return oldScrollPositionIndex != pos;
  }

  /**
   * Scrolls the scrollbar the distance given.
   * This is clamped.
   * @param distance the distance to scrollList. Positive for down, negative for up.
   * @return {@code true} iff the index currently scrolled to changed.
   */
  public boolean scrollList(int distance) {
    return this.scrollListTo(this.getListScrollPosition() + distance);
  }

  /**
   * Gets the position of the list that the scrollbar corresponds to.
   * @return the position in the list.
   */
  public int getListScrollPosition() {
    // Gotta clamp this b/c floating point math
    return MathHelper.clamp((int)((double)this.getScrollPosition() * this.listSize), 0, this.listSize - 1);
  }

  /**
   * Updates the mouse position of the scroller.
   * Call this whenever the mouse position changes.
   * @param mouseX the X coordinate of the mouse.
   * @param mouseY the Y coordinate of the mouse.
   * @return {@code true} iff the index currently scrolled to changed.
   */
  public boolean updateMousePosition(int mouseX, int mouseY) {
    int prePos = this.getListScrollPosition();
    super.updateMousePosition(mouseX, mouseY);
    return this.getListScrollPosition() != prePos;
  }

  /**
   * Handles scrollwheel events.
   * @return {@code true} iff the index currently scrolled to changed.
   */
  public boolean handleScrollWheel() {
    if (!this.isLocked()) {
      int scrollDirection = Mouse.getEventDWheel();
      if (scrollDirection != 0) {
        int scrollVal = scrollDirection > 0 ? -1 : 1;
        return this.scrollList(scrollVal);
      }
    }
    return false;
  }
}
