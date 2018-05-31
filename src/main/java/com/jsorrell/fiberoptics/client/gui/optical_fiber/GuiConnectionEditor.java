package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.OpticalFiberConnectionCreationRequest;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;

public class GuiConnectionEditor extends GuiConnectionCreator {
  private final OpticalFiberConnection oldConnection;

  public GuiConnectionEditor(BlockPos pos, OpticalFiberConnection oldConnection) {
    super(new OpticalFiberConnectionFactory(oldConnection));
    this.oldConnection = oldConnection;
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button.id == SUBMIT_BUTTON) {
      connectionFactory.setPriority(0);
      connectionFactory.setChannel("");

      OpticalFiberConnection connection;
      try {
        connection = connectionFactory.getConnection();
        // TODO set which changes
        FiberOpticsPacketHandler.INSTANCE.sendToServer(new OpticalFiberConnectionCreationRequest(connection));
      } catch (OpticalFiberConnectionFactory.NonDefiningConnectionException e) {
        assert false;
      }
      mc.displayGuiScreen(null);
    }
    super.actionPerformed(button);
  }
}
