package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketCreateConnection;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiConnectionCreator extends GuiConnectionBuilder {
  protected static final int BUTTON_WIDTH = 200;
  protected static final int BUTTON_HEIGHT = 20;
  protected static final int SUBMIT_BUTTON = 0;
  protected static final int CANCEL_BUTTON = 1;


  public GuiConnectionCreator(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  @Override
  public void initGui() {
    buttonList.add(new GuiButton(CANCEL_BUTTON, (width-BUTTON_WIDTH)/2, (height + TEXTURE_HEIGHT)/2 - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Cancel"));
    buttonList.add(new GuiButton(SUBMIT_BUTTON, (width-BUTTON_WIDTH)/2, (height + TEXTURE_HEIGHT)/2 - 2*BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Submit"));
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
        connectionFactory.setPriority(0).setChannel("");

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
