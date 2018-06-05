package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TextureSize;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.*;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GuiConnectionChooser extends GuiOpticalFiber {
  protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/connection_list.png"), new TextureSize(206, 195));
  protected static final Rectangle CONNECTION_LIST_BOX = new Rectangle(13, 23, 157, 161);

  private static final int LIST_ELEMENT_HEIGHT = 23;
  private static final int LIST_ELEMENT_WIDTH = 157;
  private static final int MAX_LIST_ELEMENTS = 7;

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
  private int scrollPosition = 0;

  private final List<GuiListElement> listElements;
  private final ImmutableList<EnumFacing> sidesToDisplay;


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
    int actualIdx = idx - this.scrollPosition;
    assert 0 <= actualIdx && actualIdx < MAX_LIST_ELEMENTS;
    return this.textureStartY + CONNECTION_LIST_BOX.y + actualIdx * LIST_ELEMENT_HEIGHT;
  }

  private List<GuiListElement> generateListElements() {
    List<GuiListElement> out = new ArrayList<>();
    int listElementIdx = 0;
    int connectionIdx = 0;

    for (EnumFacing curSide : sidesToDisplay) {
      // add side element
      GuiSideListElement sideElement = new GuiSideListElement(listElementIdx++, curSide);
      out.add(sideElement);

      // skip connections not in sidesToDisplay
      while (connectionIdx < this.connections.size() && this.connections.get(connectionIdx).connectedSide.getIndex() < curSide.getIndex()) {
        ++connectionIdx;
      }

      // add connections for that side
      while (connectionIdx < this.connections.size() && this.connections.get(connectionIdx).connectedSide == curSide) {
        GuiConnectionListElement listElement = new GuiConnectionListElement(listElementIdx++, this.connections.get(connectionIdx));
        out.add(listElement);
        ++connectionIdx;
      }
    }
    return out;
  }

  @Override
  public void initGui() {
    this.textureStartX = (this.width - TEXTURE.size.width)/2;
    this.textureStartY = (this.height - TEXTURE.size.height)/2;

    for (int i = scrollPosition; i < this.listElements.size() && i < scrollPosition + MAX_LIST_ELEMENTS; ++i) {
      this.listElements.get(i).addButtonsToList(this.buttonList, getListElementX(), getListElementY(i));
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mc == null) return; // Is this a bug in forge? Sometimes this is called by EntityRenderer#updateCameraAndRender before GuiScreen#setWorldAndResolution

    GlStateManager.color(1F, 1F, 1F, 1F);
    /* Background */
    TEXTURE.drawTexturePart(this.mc, this, this.textureStartX, this.textureStartY);

    /* Scrollbar */
    //TODO implement

    /* List Items */
    for (int i = this.scrollPosition; i < this.listElements.size() && i < scrollPosition + MAX_LIST_ELEMENTS; ++i) {
      this.listElements.get(i).drawListElement(this.mc, mouseX, mouseY, getListElementX(), getListElementY(i), partialTicks);
    }

//    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button instanceof GuiButtonConnectionAdd) {
      this.mc.displayGuiScreen(new GuiTypeChooser(this.pos, ((GuiButtonConnectionAdd) button).listElement.side));
    } else if (button instanceof GuiButtonConnectionEdit) {
      this.mc.displayGuiScreen(new GuiConnectionEditor(this.pos, ((GuiButtonConnectionEdit) button).listElement.connection));
    } else if (button instanceof GuiButtonConnectionDelete) {
      //TODO Implement
      this.connections.remove(((GuiButtonConnectionDelete) button).listElement.connection);
      this.listElements.remove(((GuiButtonConnectionDelete) button).listElement);
      this.buttonList.clear();
      this.labelList.clear();
      this.initGui();
    }
  }
}
