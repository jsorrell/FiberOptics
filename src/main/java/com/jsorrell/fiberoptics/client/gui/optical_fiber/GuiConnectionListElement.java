package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiConnectionListElement extends GuiListElement {

  protected static final Dimension TYPE_ICON_SIZE = new Dimension(16, 16);
  protected static final int TYPE_ICON_X = 2;
  protected static final int TYPE_ICON_Y = 3;

  protected static final int DIRECTION_ICON_X = 20;
  protected static final int DIRECTION_ICON_Y = 3;
  protected static final SizedTexturePart INPUT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(17, 43), new Dimension(16, 16));
  protected static final SizedTexturePart OUTPUT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(33, 43), new Dimension(16, 16));

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
  public void addButtonsToList(List<GuiButton> buttonList, int x, int y) {
    buttonList.add(this.editButton = new GuiButtonConnectionEdit(this.id, x + EDIT_BUTTON_X, y + EDIT_BUTTON_Y, this));
    buttonList.add(this.deleteButton = new GuiButtonConnectionDelete(this.id, x + DELETE_BUTTON_X, y + DELETE_BUTTON_Y, this));
  }

  @Override
  public void drawListElement(Minecraft mc, int mouseX, int mouseY, int x, int y, float partialTicks) {
    super.drawListElement(mc, mouseX, mouseY, x, y, partialTicks);

    GlStateManager.color(1F, 1F, 1F, 1F);
    /* Type Icon */
    GlStateManager.clear(256);
    this.connection.transferType.renderItemToGui(mc, this, x + TYPE_ICON_X, y + TYPE_ICON_Y, partialTicks);
    GlStateManager.clear(256);

    GlStateManager.color(1F, 1F, 1F, 1F);
    /* Direction Icon */
    if (this.connection.getTransferDirection() == OpticalFiberConnection.TransferDirection.INPUT) {
      INPUT_ICON.drawTexturePart(mc, this, x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y);
    } else {
      OUTPUT_ICON.drawTexturePart(mc, this, x + DIRECTION_ICON_X, y + DIRECTION_ICON_Y);
    }

    /* Channel Name */
    // TODO whats this do?
//    GlStateManager.enableBlend();
//    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
}
