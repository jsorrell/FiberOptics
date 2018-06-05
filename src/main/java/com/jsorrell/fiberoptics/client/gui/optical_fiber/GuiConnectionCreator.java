package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketCreateConnection;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TextureSize;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiConnectionCreator extends GuiConnectionBuilder {
  protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new TextureSize(206, 195));
  protected static final int BUTTON_WIDTH = 200;
  protected static final int BUTTON_HEIGHT = 20;
  protected static final int SUBMIT_BUTTON = 0;
  protected static final int CANCEL_BUTTON = 1;

  private int textureStartX;
  private int textureStartY;


  public GuiConnectionCreator(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    TEXTURE.drawTexturePart(this.mc, this, this.textureStartX, this.textureStartY);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public void initGui() {
    this.textureStartX = (this.width - TEXTURE.size.width)/2;
    this.textureStartY = (this.height - TEXTURE.size.height)/2;

    buttonList.add(new GuiButton(CANCEL_BUTTON, (width-BUTTON_WIDTH)/2, this.textureStartY + TEXTURE.size.height - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Cancel"));
    buttonList.add(new GuiButton(SUBMIT_BUTTON, (width-BUTTON_WIDTH)/2, this.textureStartY + TEXTURE.size.height - 2*BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Submit"));
  }

  @Override
  public void actionPerformed(@Nullable GuiButton button) {
    if (button == null) return;
    switch (button.id) {
      case CANCEL_BUTTON: {
        mc.displayGuiScreen(null);
        break;
      }
      case SUBMIT_BUTTON: {
        connectionFactory.setPriority(0).setChannel("Channel");

        OpticalFiberConnection connection;
        try {
          connection = connectionFactory.getConnection();
          FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketCreateConnection(connection));
        } catch (OpticalFiberConnectionFactory.NonDefiningConnectionException e) {
          assert false;
        }
        mc.displayGuiScreen(null);
        break;
      }
    }
  }
}
