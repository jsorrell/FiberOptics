package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketCreateConnection;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiConnectionCreator extends GuiConnectionBuilder {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));
  protected static final int BUTTON_WIDTH = 200;
  protected static final int BUTTON_HEIGHT = 20;
  protected static final int SUBMIT_BUTTON = 0;
  protected static final int CANCEL_BUTTON = 1;

  public GuiConnectionCreator(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  @Override
  public void initGui() {
    super.initGui();
    buttonList.add(new GuiButton(CANCEL_BUTTON, (width-BUTTON_WIDTH)/2, this.backgroundStart.y + BACKGROUND.size.height - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Cancel"));
    buttonList.add(new GuiButton(SUBMIT_BUTTON, (width-BUTTON_WIDTH)/2, this.backgroundStart.y + BACKGROUND.size.height - 2*BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Submit"));
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
