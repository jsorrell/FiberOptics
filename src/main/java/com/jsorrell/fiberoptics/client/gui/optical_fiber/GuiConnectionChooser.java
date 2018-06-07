package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketRemoveConnection;
import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GuiConnectionChooser extends GuiOpticalFiber {
  protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/connection_list.png"), new Dimension(206, 195));
  protected static final Rectangle CONNECTION_LIST_BOX = new Rectangle(13, 23, 157, 161);

  protected static final int LIST_ELEMENT_HEIGHT = 23;
  protected static final int MAX_LIST_ELEMENTS = 7;

  protected static final Rectangle SCROLLER_BOX = new Rectangle(181, 23, 12, 161);

  private static final Comparator<OpticalFiberConnection> CONNECTION_ORDER = (t0, t1) -> {
    int cmp;
    cmp = t0.connectedSide.compareTo(t1.connectedSide);
    if (cmp != 0) return cmp;
    cmp = Integer.compare(ModTransferTypes.getIndex(t0.transferType), ModTransferTypes.getIndex(t1.transferType));
    if (cmp != 0) return cmp;
    cmp = t0.channelName.compareTo(t1.channelName);
    if (cmp != 0) return cmp;
    cmp = t0.getTransferDirection().compareTo(t1.getTransferDirection());
    return cmp;
  };

  private final List<OpticalFiberConnection> connections;

  private final List<GuiListElement> listElements;
  private final ImmutableList<EnumFacing> sidesToDisplay;

  @Nullable
  private GuiConnectionChooserScroller scroller = null;
  private int textureStartX;
  private int textureStartY;

  /* If side is not null, we have a single side add element then connections.
   * If side is null, we have a side element for each side and connections for these sides between.
   */
  public GuiConnectionChooser(BlockPos pos, List<EnumFacing> sidesToDisplay, Collection<OpticalFiberConnection> connectionsUnsorted) {
    super(pos);
    this.connections = new ArrayList<>(connectionsUnsorted);
    this.connections.sort(CONNECTION_ORDER);
    this.sidesToDisplay = ImmutableList.sortedCopyOf(sidesToDisplay);
    this.listElements = generateListElements();
  }

  private int getListElementX() {
    return this.textureStartX + CONNECTION_LIST_BOX.x;
  }

  private int getListElementY(int idx) {
    int actualIdx = idx - this.getTopListElement();
    assert 0 <= actualIdx && actualIdx < MAX_LIST_ELEMENTS;
    return this.textureStartY + CONNECTION_LIST_BOX.y + actualIdx * LIST_ELEMENT_HEIGHT;
  }

  private List<GuiListElement> generateListElements() {
    List<GuiListElement> out = new ArrayList<>();
    int connectionIdx = 0;

    for (EnumFacing curSide : sidesToDisplay) {
      // add side element
      GuiSideListElement sideElement = new GuiSideListElement(0, curSide);
      out.add(sideElement);

      // skip connections not in sidesToDisplay
      while (connectionIdx < this.connections.size() && this.connections.get(connectionIdx).connectedSide.getIndex() < curSide.getIndex()) {
        ++connectionIdx;
      }

      // add connections for that side
      while (connectionIdx < this.connections.size() && this.connections.get(connectionIdx).connectedSide == curSide) {
        GuiConnectionListElement listElement = new GuiConnectionListElement(0, this.connections.get(connectionIdx));
        out.add(listElement);
        ++connectionIdx;
      }
    }
    return out;
  }

  @Override
  public void initGui() {
    super.initGui();

    this.textureStartX = (this.width - TEXTURE.size.width)/2;
    this.textureStartY = (this.height - TEXTURE.size.height)/2;

    /* Init List Elements */
    for (int i = this.getTopListElement(); i <= this.getBottomListElement(); ++i) {
      this.buttonList.addAll(this.listElements.get(i).initListElement(getListElementX(), getListElementY(i)));
    }

    /* Create Scroller */
    int scrollX = textureStartX + SCROLLER_BOX.x;
    int scrollY = textureStartY + SCROLLER_BOX.y;
    if (this.scroller != null) {
      this.scroller.setPos(scrollX, scrollY);
    } else {
      this.scroller = new GuiConnectionChooserScroller(0, textureStartX + SCROLLER_BOX.x, textureStartY + SCROLLER_BOX.y, AADirection2D.DOWN, SCROLLER_BOX.height, this.getNumScrollPositions());
      this.scroller.scrollTo(0F);
      this.scroller.setLocked(!this.needsScrolling());
    }
  }

  private void removeListElement(GuiListElement listElement) {
    assert this.listElements.contains(listElement);

    int deletedIndex = this.listElements.indexOf(listElement);
    int preScrollPosition = this.getTopListElement();

    this.listElements.remove(deletedIndex);

    if (this.scroller != null) {
      this.scroller.setListSize(getNumScrollPositions(), true);

      if (this.listElements.size() <= MAX_LIST_ELEMENTS) {
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
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mc == null) return; // Is this a bug in forge? Sometimes this is called by EntityRenderer#updateCameraAndRender before GuiScreen#setWorldAndResolution

    if (this.scroller != null && this.scroller.updateMousePosition(mouseX, mouseY)) {
      this.buttonList.clear();
      this.initGui();
    }

    GlStateManager.color(1F, 1F, 1F, 1F);

    /* Background */
    TEXTURE.drawTexturePart(this.mc, this, this.textureStartX, this.textureStartY);

    /* Scrollbar */
    if (this.scroller != null) {
      this.scroller.drawScroller(this.mc, mouseX, mouseY, partialTicks);
    }

    /* List Items */
    for (int i = this.getTopListElement(); i <= getBottomListElement(); ++i) {
      this.listElements.get(i).drawListElement(this.mc, mouseX, mouseY, getListElementX(), getListElementY(i), partialTicks);
    }
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button instanceof GuiButtonConnectionAdd) {
      this.mc.displayGuiScreen(new GuiTypeChooser(this.pos, ((GuiButtonConnectionAdd) button).listElement.side));
    } else if (button instanceof GuiButtonConnectionEdit) {
      this.mc.displayGuiScreen(new GuiConnectionEditor(this.pos, ((GuiButtonConnectionEdit) button).listElement.connection));
    } else if (button instanceof GuiButtonConnectionDelete) {
      GuiConnectionListElement listElement = ((GuiButtonConnectionDelete) button).listElement;
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketRemoveConnection(listElement.connection));
      this.connections.remove(listElement.connection);
      this.removeListElement(listElement);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (SCROLLER_BOX.contains(mouseX - this.textureStartX, mouseY - this.textureStartY) && this.scroller != null)
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

  private int getOverflowSize() {
    return needsScrolling() ? this.listElements.size() - MAX_LIST_ELEMENTS : 0;
  }

  private int getNumScrollPositions() {
    return getOverflowSize() + 1;
  }

  private boolean needsScrolling() {
    return this.listElements.size() > MAX_LIST_ELEMENTS;
  }

  private int getTopListElement() {
    if (this.scroller == null) return 0;
    return this.scroller.getListScrollPosition();
  }

  private int getBottomListElement() {
    return (this.needsScrolling() ? getTopListElement() + MAX_LIST_ELEMENTS : this.listElements.size()) - 1;
  }

  private boolean isScrolledToBottom() {
    return !needsScrolling() || getTopListElement() == getOverflowSize();
  }
}
