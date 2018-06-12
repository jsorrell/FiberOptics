package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.gui.components.GuiListScroller;
import com.jsorrell.fiberoptics.client.gui.components.IGuiListElement;
import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TexturePart;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Rectangle;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class GuiOpticalFiberListWithScroller extends GuiOpticalFiber {
  final Rectangle listElementBox;
  final int listElementHeight;
  final Rectangle scrollerBox;

  GuiListScroller scroller;

  final List<IGuiListElement> listElements;

  public GuiOpticalFiberListWithScroller(Rectangle scrollerBox, Rectangle listElementBox, int listElementHeight, Collection<IGuiListElement> listElements) {
    this.scrollerBox = scrollerBox;
    this.listElementBox = listElementBox;
    this.listElementHeight = listElementHeight;
    this.listElements = new ArrayList<>(listElements);
  }

  @Override
  public void initGui() {
    super.initGui();

    /* Init List Elements */
    for (int i = this.getTopListElement(); i <= this.getBottomListElement(); ++i) {
      this.buttonList.addAll(this.listElements.get(i).initListElement(i, getListElementX(), getListElementY(i)));
    }

    /* Create Scroller */
    int scrollX = this.backgroundStart.getX() + this.scrollerBox.getX();
    int scrollY = this.backgroundStart.getY() + this.scrollerBox.getY();
    if (this.scroller != null) {
      this.scroller.setPos(scrollX, scrollY);
    } else {
      this.scroller = new GuiOpticalFiberListScroller(0, backgroundStart.getX() + this.scrollerBox.getX(), backgroundStart.getY() + this.scrollerBox.getY(), AADirection2D.DOWN, this.scrollerBox.getHeight(), this.getNumScrollPositions());
      this.scroller.scrollTo(0F);
      this.scroller.setLocked(!this.needsScrolling());
    }
  }

  @Override
  public void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    if (this.scroller != null && this.scroller.updateMousePosition(mouseX, mouseY)) {
      this.buttonList.clear();
      this.initGui();
    }

    GlStateManager.color(1, 1, 1, 1);

    /* Background */
    this.drawBackground();

    /* List Items */
    for (int i = this.getTopListElement(); i <= getBottomListElement(); ++i) {
      this.listElements.get(i).drawListElement(this.mc, mouseX, mouseY, partialTicks);
    }

    /* Scrollbar */
    if (this.scroller != null) {
      this.scroller.drawScroller(this.mc, mouseX, mouseY, partialTicks);
    }

    /* Buttons */
    super.drawButtonsAndLabels(mouseX, mouseY, partialTicks);
  }

  @Nullable
  @Override
  public abstract SizedTexturePart getBackgroundTexture();


  private int getListElementX() {
    return this.backgroundStart.getX() + this.listElementBox.getX();
  }

  private int getListElementY(int idx) {
    int actualIdx = idx - this.getTopListElement();
    assert 0 <= actualIdx && actualIdx < this.getMaxListElements();
    return this.backgroundStart.getY() + this.listElementBox.getY() + actualIdx * this.listElementHeight;
  }


  public void removeListElement(IGuiListElement listElement) {
    assert this.listElements.contains(listElement);

    int deletedIndex = this.listElements.indexOf(listElement);
    int preScrollPosition = this.getTopListElement();

    this.listElements.remove(deletedIndex);

    if (this.scroller != null) {
      this.scroller.setListSize(getNumScrollPositions(), true);

      if (this.listElements.size() <= this.getMaxListElements()) {
        this.scroller.setLocked(true);
        this.scroller.scrollListTo(0);
      } else {
        if (deletedIndex < preScrollPosition) {
          // Deleted index above ours. Indices shift up.
          this.scroller.scrollList(-1);
        }
      }
    }
    this.buttonList.clear();
    this.initGui();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (this.scrollerBox.contains(mouseX - backgroundStart.getX(), mouseY - backgroundStart.getY()) && this.scroller != null)
      this.scroller.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    super.mouseReleased(mouseX, mouseY, state);
    if (this.scroller != null)
      this.scroller.mouseReleased(mouseX, mouseY, state);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    if (this.scroller != null && this.scroller.handleScrollWheel()) {
      this.buttonList.clear();
      this.initGui();
    }
  }

  private int getMaxListElements() {
    return this.listElementBox.getHeight() / this.listElementHeight;
  }

  private int getOverflowSize() {
    return needsScrolling() ? this.listElements.size() - this.getMaxListElements() : 0;
  }

  private int getNumScrollPositions() {
    return getOverflowSize() + 1;
  }

  private boolean needsScrolling() {
    return this.listElements.size() > this.getMaxListElements();
  }

  private int getTopListElement() {
    if (this.scroller == null) return 0;
    return this.scroller.getListScrollPosition();
  }

  private int getBottomListElement() {
    return (this.needsScrolling() ? getTopListElement() + this.getMaxListElements() : this.listElements.size()) - 1;
  }

  private boolean isScrolledToBottom() {
    return !needsScrolling() || getTopListElement() == getOverflowSize();
  }

  public static class GuiOpticalFiberListScroller extends GuiListScroller {
    protected static final Dimension SIZE = new Dimension(12,15);
    protected static final TexturePart TEXTURE = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(0, 0));
    protected static final TexturePart TEXTURE_LOCKED = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(12, 0));
    public GuiOpticalFiberListScroller(int id, int x, int y, AADirection2D scrollDirection, int scrollHeight, int listSize) {
      super(id, x, y, scrollDirection, scrollHeight, SIZE, TEXTURE, TEXTURE_LOCKED, listSize);
    }
  }
}
