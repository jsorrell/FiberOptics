package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GuiConnectionChooser extends GuiOpticalFiber {
  private static final int MAX_BUTTONS = 12;
  private static final int CONNECTION_BUTTON_HEIGHT = 20;
  private static final int CONNECTION_BUTTON_WIDTH = 200;

  private static final Comparator<OpticalFiberConnection> CONNECTION_ORDER = (t0, t1) -> {
    int cmp;
    // Provide consistency with equals
    cmp = t0.pos.compareTo(t1.pos);
    if (cmp != 0) return cmp;
    cmp = t0.connectedSide.compareTo(t1.connectedSide);
    if (cmp != 0) return cmp;
    cmp = Integer.compare(ModTransferTypes.getIndex(t0.transferType), ModTransferTypes.getIndex(t1.transferType));
    if (cmp != 0) return cmp;
    cmp = t0.channelName.compareTo(t1.channelName);
    return cmp;
  };

  private final EnumFacing side;
  private final List<OpticalFiberConnection> connections;
  private final int numConnectionsButtons;
  private final GuiButton[] connectionsButtons;
  private GuiButton createConnectionButton;
  private int scrollPosition = 0;

  public GuiConnectionChooser(BlockPos pos, @Nullable EnumFacing side, Collection<OpticalFiberConnection> connections) {
    super(pos);
    this.side = side;
    this.connections = ImmutableList.sortedCopyOf(CONNECTION_ORDER, connections);
    // Can have one more connection if we don't have add button
    numConnectionsButtons = side == null ? Integer.min(MAX_BUTTONS, this.connections.size()) : Integer.min(MAX_BUTTONS-1, this.connections.size());
    connectionsButtons = new GuiButton[numConnectionsButtons];
  }

  @Override
  public void initGui() {
    int buttonYStart = (height-MAX_BUTTONS*CONNECTION_BUTTON_HEIGHT)/2;
    for (int i = scrollPosition; i < numConnectionsButtons; i++) {
      OpticalFiberConnection connection = this.connections.get(i);
      String buttonText = connection.connectedSide + " " + connection.transferType + " " + connection.getTransferDirection();
      this.connectionsButtons[i-scrollPosition] = new GuiButton(i, width/2 - CONNECTION_BUTTON_WIDTH/2, buttonYStart + i*CONNECTION_BUTTON_HEIGHT, CONNECTION_BUTTON_WIDTH, CONNECTION_BUTTON_HEIGHT, buttonText);
      buttonList.add(this.connectionsButtons[i-scrollPosition]);
    }
    if (side != null) {
      createConnectionButton = new GuiButton(numConnectionsButtons, (width-CONNECTION_BUTTON_WIDTH)/2, buttonYStart + numConnectionsButtons*CONNECTION_BUTTON_HEIGHT, CONNECTION_BUTTON_WIDTH, CONNECTION_BUTTON_HEIGHT, "Create");
      buttonList.add(createConnectionButton);
    }
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button.id == numConnectionsButtons) {
      assert this.side != null;
      this.mc.displayGuiScreen(new GuiTypeChooser(this.pos, this.side));
    } else {
      OpticalFiberConnection connection = this.connections.get(button.id);
      assert connection != null;
      this.mc.displayGuiScreen(new GuiConnectionEditor(this.pos, connection));
    }
  }
}
