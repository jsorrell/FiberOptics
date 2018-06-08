package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.gui.components.GuiListElement;
import com.jsorrell.fiberoptics.client.gui.components.GuiListScroller;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketRemoveConnection;
import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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
public class GuiScreenConnectionChooser extends GuiOpticalFiber {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/connection_list.png"), new Dimension(206, 195));
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

  /* If side is not null, we have a single side add element then connections.
   * If side is null, we have a side element for each side and connections for these sides between.
   */
  public GuiScreenConnectionChooser(BlockPos pos, List<EnumFacing> sidesToDisplay, Collection<OpticalFiberConnection> connectionsUnsorted) {
    super(pos);
    this.connections = new ArrayList<>(connectionsUnsorted);
    this.connections.sort(CONNECTION_ORDER);
    this.sidesToDisplay = ImmutableList.sortedCopyOf(sidesToDisplay);
    this.listElements = generateListElements();
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  private int getListElementX() {
    return this.backgroundStart.x + CONNECTION_LIST_BOX.x;
  }

  private int getListElementY(int idx) {
    int actualIdx = idx - this.getTopListElement();
    assert 0 <= actualIdx && actualIdx < MAX_LIST_ELEMENTS;
    return this.backgroundStart.y + CONNECTION_LIST_BOX.y + actualIdx * LIST_ELEMENT_HEIGHT;
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
  public void initGui() {
    super.initGui();

    /* Init List Elements */
    for (int i = this.getTopListElement(); i <= this.getBottomListElement(); ++i) {
      this.buttonList.addAll(this.listElements.get(i).initListElement(getListElementX(), getListElementY(i)));
    }

    /* Create Scroller */
    int scrollX = this.backgroundStart.x + SCROLLER_BOX.x;
    int scrollY = this.backgroundStart.y + SCROLLER_BOX.y;
    if (this.scroller != null) {
      this.scroller.setPos(scrollX, scrollY);
    } else {
      this.scroller = new GuiConnectionChooserScroller(0, backgroundStart.x + SCROLLER_BOX.x, backgroundStart.y + SCROLLER_BOX.y, AADirection2D.DOWN, SCROLLER_BOX.height, this.getNumScrollPositions());
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

    GlStateManager.color(1F, 1F, 1F, 1F);

    /* Background */
    this.drawBackground();
    BACKGROUND.drawTexturePart(this.mc, backgroundStart.x, backgroundStart.y, this.zLevel);

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
    if (button instanceof GuiSideListElement.GuiButtonConnectionAdd) {
      this.mc.displayGuiScreen(new GuiScreenTypeChooser(this.pos, ((GuiSideListElement.GuiButtonConnectionAdd) button).listElement.side, Arrays.asList(ModTransferTypes.VALUES)));
    } else if (button instanceof GuiConnectionListElement.GuiButtonConnectionEdit) {
      OpticalFiberConnection connection = ((GuiConnectionListElement.GuiButtonConnectionEdit) button).listElement.connection;
      connection.transferType.displayEditConnectionGui(this.mc, connection);
    } else if (button instanceof GuiConnectionListElement.GuiButtonConnectionDelete) {
      GuiConnectionListElement listElement = ((GuiConnectionListElement.GuiButtonConnectionDelete) button).listElement;
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketRemoveConnection(listElement.connection));
      this.connections.remove(listElement.connection);
      this.removeListElement(listElement);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (SCROLLER_BOX.contains(mouseX - backgroundStart.x, mouseY - backgroundStart.y) && this.scroller != null)
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

  private static class GuiConnectionListElement extends GuiListElement {

    protected static final Dimension TYPE_ICON_SIZE = new Dimension(16, 16);
    protected static final int TYPE_ICON_X = 2;
    protected static final int TYPE_ICON_Y = 3;

    protected static final int DIRECTION_ICON_X = 20;
    protected static final int DIRECTION_ICON_Y = 3;
    protected static final SizedTexturePart INSERT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(17, 43), new Dimension(16, 16));
    protected static final SizedTexturePart EXTRACT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(33, 43), new Dimension(16, 16));

    protected static final int CHANNEL_NAME_X = 39;
    protected static final int MAX_CHANNEL_NAME_WIDTH = 77;
    protected static final int CHANNEL_NAME_COLOR = 0x404040;

    protected static final int EDIT_BUTTON_X = 119;
    protected static final int EDIT_BUTTON_Y = 3;

    protected static final int DELETE_BUTTON_X = 137;
    protected static final int DELETE_BUTTON_Y = 3;

    public final OpticalFiberConnection connection;
    public GuiButtonConnectionEdit editButton = null;
    public GuiButtonConnectionDelete deleteButton = null;

    public GuiConnectionListElement(int id, OpticalFiberConnection connection) {
      super(id);
      this.connection = connection;
    }

    @Override
    public Collection<GuiButton> initListElement(int x, int y) {

      this.editButton = new GuiButtonConnectionEdit(this.id, x + EDIT_BUTTON_X, y + EDIT_BUTTON_Y, this);
      this.deleteButton = new GuiButtonConnectionDelete(this.id, x + DELETE_BUTTON_X, y + DELETE_BUTTON_Y, this);
      return ImmutableList.of(this.editButton, this.deleteButton);
    }

    @Override
    public void drawListElement(Minecraft mc, int mouseX, int mouseY, int x, int y, float partialTicks) {
      super.drawListElement(mc, mouseX, mouseY, x, y, partialTicks);

      GlStateManager.color(1F, 1F, 1F, 1F);
      /* Type Icon */
      GlStateManager.clear(256);
      this.connection.transferType.drawTypeIcon(mc, x + TYPE_ICON_X, y + TYPE_ICON_Y, this.zLevel, partialTicks);
      GlStateManager.clear(256);

      GlStateManager.color(1F, 1F, 1F, 1F);
      /* Direction Icon */
      if (this.connection.getTransferDirection() == OpticalFiberConnection.TransferDirection.EXTRACT) {
        EXTRACT_ICON.drawTexturePart(mc,x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y, this.zLevel);
      } else {
        INSERT_ICON.drawTexturePart(mc, x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y, this.zLevel);
      }

      /* Channel Name */
      String channelNameToRender = this.connection.channelName.isEmpty() ? "[unnamed]" : this.connection.channelName;
      if (mc.fontRenderer.getStringWidth(channelNameToRender) > MAX_CHANNEL_NAME_WIDTH) {
        StringBuilder channelNameBuilder = new StringBuilder();
        int chars = 0;
        int length = 3 * mc.fontRenderer.getCharWidth('.');
        char character = this.connection.channelName.charAt(0);
        int charSize = mc.fontRenderer.getCharWidth(character);

        while (length <= MAX_CHANNEL_NAME_WIDTH) {
          channelNameBuilder.append(character);
          length += charSize;

          charSize = mc.fontRenderer.getCharWidth(character = this.connection.channelName.charAt(++chars));
        }
        channelNameBuilder.append('.').append('.').append('.');
        channelNameToRender = channelNameBuilder.toString();
      }

      int channelNameX = x + CHANNEL_NAME_X;
      int channelNameY = y + (TEXTURE.size.height - mc.fontRenderer.FONT_HEIGHT)/2;
      mc.fontRenderer.drawString(channelNameToRender, channelNameX, channelNameY, CHANNEL_NAME_COLOR);

      if (this.editButton != null && this.deleteButton != null) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.editButton.drawButton(mc, mouseX, mouseY, partialTicks);
        this.deleteButton.drawButton(mc, mouseX, mouseY, partialTicks);
      }
    }

    private static class GuiButtonConnectionDelete extends GuiButtonImage {
      protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(239, 26), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiConnectionListElement listElement;

      public GuiButtonConnectionDelete(int id, int x, int y, GuiConnectionListElement listElement) {
        super(id, x, y, TEXTURE.size.width, TEXTURE.size.height, TEXTURE.offset.textureOffsetX, TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, TEXTURE.texture);
        this.listElement = listElement;
      }
    }

    private static class GuiButtonConnectionEdit extends GuiButtonImage {
      protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(222, 26), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiConnectionListElement listElement;

      public GuiButtonConnectionEdit(int id, int x, int y, GuiConnectionListElement listElement) {
        super(id, x, y, TEXTURE.size.width, TEXTURE.size.height, TEXTURE.offset.textureOffsetX, TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, TEXTURE.texture);
        this.listElement = listElement;
      }
    }
  }

  public static class GuiSideListElement extends GuiListElement {
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

    private static class GuiButtonConnectionAdd extends GuiButtonImage {
      protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(205, 26), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiSideListElement listElement;

      public GuiButtonConnectionAdd(int id, int x, int y, GuiSideListElement listElement) {
        super(id, x, y, TEXTURE.size.width, TEXTURE.size.height, TEXTURE.offset.textureOffsetX, TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, TEXTURE.texture);
        this.listElement = listElement;
      }
    }
  }

  public static class GuiConnectionChooserScroller extends GuiListScroller {
    protected static final Dimension SIZE = new Dimension(12,15);
    protected static final TexturePart TEXTURE = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(232, 0));
    protected static final TexturePart TEXTURE_LOCKED = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(244, 0));
    public GuiConnectionChooserScroller(int id, int x, int y, AADirection2D scrollDirection, int scrollHeight, int listSize) {
      super(id, x, y, scrollDirection, scrollHeight, SIZE, TEXTURE, TEXTURE_LOCKED, listSize);
    }
  }
}
