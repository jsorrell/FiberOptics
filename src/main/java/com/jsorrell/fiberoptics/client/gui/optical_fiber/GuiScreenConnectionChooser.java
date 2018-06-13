package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.gui.components.IGuiListElement;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenChannelChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketRemoveConnection;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Rectangle;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GuiScreenConnectionChooser extends GuiOpticalFiberListWithScroller {
  private static final ResourceLocation CONNECTION_CHOOSER_LOCATION = new ResourceLocation(FiberOptics.MODID, "textures/gui/connection_chooser.png");
  private static final SizedTexturePart BACKGROUND = new SizedTexturePart(CONNECTION_CHOOSER_LOCATION, new Dimension(206, 195));
  protected static final SizedTexturePart ELEMENT_BACKGROUND = new SizedTexturePart(CONNECTION_CHOOSER_LOCATION, new TextureOffset(0, 195), new Dimension(157, 23));
  private static final Rectangle SCROLLER_BOX = new Rectangle(181, 23, 12, 161);
  private static final Rectangle LIST_ELEMENT_BOX = new Rectangle(13, 23, 157, 161);

  protected BlockPos pos;

  public GuiScreenConnectionChooser (BlockPos pos, Collection<EnumFacing> sidesToDisplay, Collection<OpticalFiberConnection> connections) {
    super(SCROLLER_BOX, LIST_ELEMENT_BOX, ELEMENT_BACKGROUND.size.getHeight(), generateListElements(sidesToDisplay, connections));
    connections.forEach(c -> System.out.println(c.pos ));
    this.pos = pos;
  }

  private static List<IGuiListElement> generateListElements(Collection<EnumFacing> sidesToDisplay, Collection<OpticalFiberConnection> connectionsUnsorted) {
    List<OpticalFiberConnection> connections = new ArrayList<>(connectionsUnsorted);
    connections.sort(GuiScreenConnectionChooser::connectionOrder);

    List<IGuiListElement> listElements = new ArrayList<>();
    int connectionIdx = 0;

    for (EnumFacing curSide : sidesToDisplay) {
      // add side element
      GuiSideListElement sideElement = new GuiSideListElement(curSide);
      listElements.add(sideElement);

      // skip connections not in sidesToDisplay
      while (connectionIdx < connections.size() && connections.get(connectionIdx).side.getIndex() < curSide.getIndex()) {
        ++connectionIdx;
      }

      // add connections for that side
      while (connectionIdx < connections.size() && connections.get(connectionIdx).side == curSide) {
        GuiConnectionListElement listElement = new GuiConnectionListElement(connections.get(connectionIdx));
        listElements.add(listElement);
        ++connectionIdx;
      }
    }
    return listElements;
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button instanceof GuiSideListElement.GuiButtonConnectionAdd) {
      // Add
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketOpenChannelChooser.Request(this.pos, ((GuiSideListElement.GuiButtonConnectionAdd) button).listElement.side));
    } else if (button instanceof GuiConnectionListElement.GuiButtonConnectionEdit) {
      // Edit
      OpticalFiberConnection connection = ((GuiConnectionListElement.GuiButtonConnectionEdit) button).listElement.connection;
      connection.getTransferType().displayEditConnectionGui(this.mc, connection);
    } else if (button instanceof GuiConnectionListElement.GuiButtonConnectionDelete) {
      // Delete
      GuiConnectionListElement listElement = ((GuiConnectionListElement.GuiButtonConnectionDelete) button).listElement;
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketRemoveConnection(listElement.connection));
      this.removeListElement(listElement);
    }
  }

  private static int connectionOrder(OpticalFiberConnection t0, OpticalFiberConnection t1) {
    int cmp;
    // This needs to be first
    if ((cmp = t0.side.compareTo(t1.side)) != 0) return cmp;
    // Group by able to connect. Order within doesn't matter.
    if ((cmp = t0.channelName.compareTo(t1.channelName)) != 0) return cmp;
    if ((cmp = TransferType.getKeyFromType(t0.getTransferType()).compareTo(TransferType.getKeyFromType(t1.getTransferType()))) != 0) return cmp;
    // Sort remaining. Connection type should probably be first.
    if ((cmp = t0.getTransferType().getKeyFromConnection(t0.getClass()).compareTo(t1.getTransferType().getKeyFromConnection(t1.getClass()))) != 0) return cmp;

    return 0;
  }

  /* Connection List Element */
  private static class GuiConnectionListElement extends Gui implements IGuiListElement {
    protected static final Dimension TYPE_ICON_SIZE = new Dimension(16, 16);
    protected static final int TYPE_ICON_X = 2;
    protected static final int TYPE_ICON_Y = 3;

    protected static final int DIRECTION_ICON_X = 20;
    protected static final int DIRECTION_ICON_Y = 3;

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

    public GuiConnectionListElement(OpticalFiberConnection connection) {
      this.connection = connection;
    }

    protected int x, y;

    @Override
    public List<GuiButton> initListElement(int elementNum, int x, int y) {
      this.x = x;
      this.y = y;
      this.editButton = new GuiButtonConnectionEdit(elementNum, x + EDIT_BUTTON_X, y + EDIT_BUTTON_Y, this);
      this.deleteButton = new GuiButtonConnectionDelete(elementNum, x + DELETE_BUTTON_X, y + DELETE_BUTTON_Y, this);
      return ImmutableList.of(this.editButton, this.deleteButton);
    }

    @Override
    public void drawListElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      /* Background */
      ELEMENT_BACKGROUND.drawTexturePart(mc, this.x, this.y, partialTicks);

      /* Type Icon */
      GlStateManager.clear(256);
      this.connection.getTransferType().drawTypeIcon(mc, x + TYPE_ICON_X, y + TYPE_ICON_Y, this.zLevel, TYPE_ICON_SIZE, partialTicks);
      GlStateManager.clear(256);

      /* Direction Icon */
      //FIXME
//      INSERT_ICON.drawTexturePart(mc, x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y, this.zLevel);
//      this.connection.drawConnectionIcon(mc, x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y, this.zLevel);

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
      int channelNameY = y + (ELEMENT_BACKGROUND.size.getHeight() - mc.fontRenderer.FONT_HEIGHT)/2;
      mc.fontRenderer.drawString(channelNameToRender, channelNameX, channelNameY, CHANNEL_NAME_COLOR);

      if (this.editButton != null && this.deleteButton != null) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        this.editButton.drawButton(mc, mouseX, mouseY, partialTicks);
        this.deleteButton.drawButton(mc, mouseX, mouseY, partialTicks);
      }
    }

    private static class GuiButtonConnectionDelete extends GuiButtonImage {
      protected static final SizedTexturePart BUTTON_TEXTURE = new SizedTexturePart(CONNECTION_CHOOSER_LOCATION, new TextureOffset(206, 34), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiConnectionListElement listElement;

      public GuiButtonConnectionDelete(int id, int x, int y, GuiConnectionListElement listElement) {
        super(id, x, y, BUTTON_TEXTURE.size.getWidth(), BUTTON_TEXTURE.size.getHeight(), BUTTON_TEXTURE.offset.textureOffsetX, BUTTON_TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, BUTTON_TEXTURE.texture);
        this.listElement = listElement;
      }
    }

    private static class GuiButtonConnectionEdit extends GuiButtonImage {
      protected static final SizedTexturePart BUTTON_TEXTURE = new SizedTexturePart(CONNECTION_CHOOSER_LOCATION, new TextureOffset(223, 0), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiConnectionListElement listElement;

      public GuiButtonConnectionEdit(int id, int x, int y, GuiConnectionListElement listElement) {
        super(id, x, y, BUTTON_TEXTURE.size.getWidth(), BUTTON_TEXTURE.size.getHeight(), BUTTON_TEXTURE.offset.textureOffsetX, BUTTON_TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, BUTTON_TEXTURE.texture);
        this.listElement = listElement;
      }
    }
  }

  /* Side List Element */
  public static class GuiSideListElement extends Gui implements IGuiListElement {
    protected static final int CREATE_BUTTON_X = 137;
    protected static final int CREATE_BUTTON_Y = 3;

    protected static final int SIDE_NAME_X = 6;
    protected static final float SIDE_NAME_SCALE_FACTOR = 1.5F;
    protected static final int SIDE_NAME_COLOR = 0x404040;

    public final EnumFacing side;
    public GuiButtonConnectionAdd addButton = null;

    protected int x, y;

    public GuiSideListElement(EnumFacing side) {
      this.side = side;
    }

    @Override
    public List<GuiButton> initListElement(int elementNum, int x, int y) {
      this.x = x;
      this.y = y;
      List<GuiButton> buttons = new ArrayList<>();
      buttons.add(addButton = new GuiButtonConnectionAdd(0, x + CREATE_BUTTON_X, y + CREATE_BUTTON_Y, this));
      return buttons;
    }

    @Override
    public void drawListElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      GlStateManager.color(1, 1, 1, 1);
      /* Background */
      ELEMENT_BACKGROUND.drawTexturePart(mc, this.x, this.y, partialTicks);

      /* Side Name */
      String sideName = I18n.format("enumFacing." + this.side.getName() + ".name");
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + SIDE_NAME_X, y + (ELEMENT_BACKGROUND.size.getHeight() - mc.fontRenderer.FONT_HEIGHT) / 2D, 1D);
      GlStateManager.scale(SIDE_NAME_SCALE_FACTOR, SIDE_NAME_SCALE_FACTOR, 1F);
      mc.fontRenderer.drawString(sideName, 0, 0, SIDE_NAME_COLOR);
      GlStateManager.popMatrix();

      // TODO is there a better way to do this?
      GlStateManager.color(1, 1, 1, 1);
    }

    private static class GuiButtonConnectionAdd extends GuiButtonImage {
      protected static final SizedTexturePart TEXTURE = new SizedTexturePart(CONNECTION_CHOOSER_LOCATION, new TextureOffset(206, 0), new Dimension(17, 17));
      protected static final int PRESSED_Y_OFFSET = 17;
      public final GuiSideListElement listElement;

      public GuiButtonConnectionAdd(int id, int x, int y, GuiSideListElement listElement) {
        super(id, x, y, TEXTURE.size.getWidth(), TEXTURE.size.getHeight(), TEXTURE.offset.textureOffsetX, TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, TEXTURE.texture);
        this.listElement = listElement;
      }
    }
  }
}
