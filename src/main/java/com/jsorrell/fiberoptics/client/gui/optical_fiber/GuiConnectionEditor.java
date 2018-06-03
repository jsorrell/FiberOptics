package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketCreateConnection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class GuiConnectionEditor extends GuiConnectionCreator {
  private final OpticalFiberConnection oldConnection;

  public GuiConnectionEditor(BlockPos pos, OpticalFiberConnection oldConnection) {
    super(new OpticalFiberConnectionFactory(oldConnection));
    this.oldConnection = oldConnection;
  }

  @Override
  public void actionPerformed(@Nullable GuiButton button) {
    if (button == null) return;
    if (button.id == SUBMIT_BUTTON) {
      connectionFactory.setPriority(0);
      connectionFactory.setChannel("");

      OpticalFiberConnection connection;
      try {
        connection = connectionFactory.getConnection();
        FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketCreateConnection(connection, this.oldConnection));
      } catch (OpticalFiberConnectionFactory.NonDefiningConnectionException e) {
        assert false;
      }
      mc.displayGuiScreen(null);
    }
    super.actionPerformed(button);
  }
}
