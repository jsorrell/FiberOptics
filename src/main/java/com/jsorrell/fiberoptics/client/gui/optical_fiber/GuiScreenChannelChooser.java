package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.gui.components.IGuiListElement;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenTransferTypeChooser;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GuiScreenChannelChooser extends GuiOpticalFiberListWithScroller {
  private static final ResourceLocation CHANNEL_CHOOSER_RESOURCE = new ResourceLocation(FiberOptics.MODID, "textures/gui/channel_chooser.png");
  private static final SizedTexturePart BACKGROUND = new SizedTexturePart(CHANNEL_CHOOSER_RESOURCE, new Dimension(206, 195));
  private static final Rectangle SCROLLER_BOX = new Rectangle(181, 44, 12, 140);
  private static final Rectangle LIST_ELEMENT_BOX = new Rectangle(13, 44, 157, 140);
  private static final int LIST_ELEMENT_HEIGHT = 20;
  private static final Point SUBMIT_BUTTON_POSITION = new Point(179, 13);

  private static final Rectangle CHANNEL_FIELD_BOX = new Rectangle(15, 19, 154, 9);
  private static final String DEFAULT_CHANNEL = "DEFAULT";

  final BlockPos pos;
  final EnumFacing side;

  private double channelFieldScale;
  private GuiTextField channelField;

  public GuiScreenChannelChooser(BlockPos pos, EnumFacing side, Collection<String> channels) {
    super(SCROLLER_BOX, LIST_ELEMENT_BOX, LIST_ELEMENT_HEIGHT, generateListElements(channels));
    this.pos = pos;
    this.side = side;
  }

  private static List<IGuiListElement> generateListElements(Collection<String> channels) {
    List<IGuiListElement> listElements = new ArrayList<>();

    for (String channel : channels) {
      listElements.add(new ChannelListElement(channel));
    }

    return listElements;
  }

  @Override
  public void initGui() {
    super.initGui();

    SubmitButton submitButton = new SubmitButton(0, this.backgroundStart.getX() + SUBMIT_BUTTON_POSITION.getX(), this.backgroundStart.getY() + SUBMIT_BUTTON_POSITION.getY());
    this.buttonList.add(submitButton);

    Keyboard.enableRepeatEvents(true);
    this.channelFieldScale = (double) CHANNEL_FIELD_BOX.getHeight() / (double) this.fontRenderer.FONT_HEIGHT;

    GuiTextField oldChannel = this.channelField;

    // Translated before being drawn
    this.channelField = new GuiTextField(0, this.fontRenderer, 0, 0, CHANNEL_FIELD_BOX.getWidth(), CHANNEL_FIELD_BOX.getHeight());
    this.channelField.setTextColor(-1);
    this.channelField.setDisabledTextColour(-1);
    this.channelField.setEnableBackgroundDrawing(false);
    this.channelField.setFocused(true);
    this.channelField.setCanLoseFocus(false);

    this.channelField.setText(oldChannel == null ? DEFAULT_CHANNEL : oldChannel.getText());
    this.channelField.setMaxStringLength(oldChannel == null ? 100 : oldChannel.getMaxStringLength());
    this.channelField.setCursorPosition(oldChannel == null ? DEFAULT_CHANNEL.length() : oldChannel.getCursorPosition());
    this.channelField.setSelectionPos(oldChannel == null ? 0 : oldChannel.getSelectionEnd());

  }

  @Override
  public void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    super.drawScreenUnchecked(mouseX, mouseY, partialTicks);
    if (this.channelField != null) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(this.backgroundStart.getX() + CHANNEL_FIELD_BOX.getX(), this.backgroundStart.getY() + CHANNEL_FIELD_BOX.getY(), 0);
      GlStateManager.scale(this.channelFieldScale, this.channelFieldScale, 1D);
      this.channelField.drawTextBox();
      GlStateManager.popMatrix();
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (keyCode == Keyboard.KEY_RETURN) {
      this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      this.submitChannel();
    }

    if (this.channelField.textboxKeyTyped(typedChar, keyCode)) {
      String s = this.channelField.getText();
      // Dynamically set field length to size of channel field box.
      // 6 for largest reasonable font width.
      if ((this.fontRenderer.getStringWidth(s) + 6) * this.channelFieldScale >= CHANNEL_FIELD_BOX.getWidth()) {
        this.channelField.setMaxStringLength(s.length()); // Lock the field
      } else if (s.length() == this.channelField.getMaxStringLength()) {
        this.channelField.setMaxStringLength(this.channelField.getMaxStringLength() + 1); // Unlock
      }
    } else {
      super.keyTyped(typedChar, keyCode);
    }
  }

  @Override
  @Deprecated
  public void removeListElement(IGuiListElement listElement) {
    super.removeListElement(listElement);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button instanceof ChannelListElement.ChannelButton) {
      String channelClicked = ((ChannelListElement.ChannelButton) button).listElement.channelName;
      this.channelField.setText(channelClicked);
    } else if (button instanceof SubmitButton) {
      this.submitChannel();
    }
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  public void submitChannel() {
    String channel = this.channelField.getText();
    this.channelField.setEnabled(false);
    Keyboard.enableRepeatEvents(false);
    FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketOpenTransferTypeChooser.Request(this.pos, this.side, channel));
  }

  /* Submit Button */
  private static class SubmitButton extends GuiButtonImage {
    private static final SizedTexturePart BACKGROUND = new SizedTexturePart(CHANNEL_CHOOSER_RESOURCE, new TextureOffset(206, 0), new Dimension(16, 20));

    public SubmitButton(int id, int x, int y) {
      super(id, x, y, BACKGROUND.size.getWidth(), BACKGROUND.size.getHeight(), BACKGROUND.offset.textureOffsetX, BACKGROUND.offset.textureOffsetY, BACKGROUND.size.getHeight(), BACKGROUND.texture);
    }
  }

  /* List Element */
  private static class ChannelListElement extends Gui implements IGuiListElement {
    public final String channelName;

    private ChannelListElement(String channelName) {
      this.channelName = channelName;
    }

    @Override
    public List<GuiButton> initListElement(int elementNum, int x, int y) {
      ChannelButton button = new ChannelButton(elementNum, x, y, this);
      return ImmutableList.of(button);
    }

    public static class ChannelButton extends GuiButtonImage {
      private static final SizedTexturePart BACKGROUND = new SizedTexturePart(CHANNEL_CHOOSER_RESOURCE, new TextureOffset(0, 195), new Dimension(157, 20));
      private static final Rectangle CHANNEL_OPTION_NAME_BOX = new Rectangle(3, 6, CHANNEL_FIELD_BOX.getWidth(), CHANNEL_FIELD_BOX.getHeight());
      private static final int CHANNEL_OPTION_NAME_COLOR = 0x404040;
      public final ChannelListElement listElement;

      @Override
      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x + CHANNEL_OPTION_NAME_BOX.getX(), this.y + CHANNEL_OPTION_NAME_BOX.getY(), 0);
        double scale = (double) CHANNEL_OPTION_NAME_BOX.getHeight() / (double) mc.fontRenderer.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 1D);
        mc.fontRenderer.drawString(this.listElement.channelName, 0, 0, CHANNEL_OPTION_NAME_COLOR);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
      }

      public ChannelButton(int id, int x, int y, ChannelListElement listElement) {
        super(id, x, y, LIST_ELEMENT_BOX.getWidth(), LIST_ELEMENT_HEIGHT, BACKGROUND.offset.textureOffsetX, BACKGROUND.offset.textureOffsetY, BACKGROUND.size.getHeight(), BACKGROUND.texture);
        this.listElement = listElement;
      }
    }
  }
}
