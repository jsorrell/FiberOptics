package com.jsorrell.fiberoptics.client.gui;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.OpticalFiberConnectionCreationMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.List;

public class TerminatorGui extends GuiScreen {
  private BlockPos pos;
  private List<TileOpticalFiberBase.PossibleConnection> possibleConnections;
  private GuiButton[] possibleConnectionsButtons;

  public TerminatorGui(BlockPos pos, List<TileOpticalFiberBase.PossibleConnection> possibleConnections) {
    this.pos = pos;
    this.possibleConnections = possibleConnections;
    this.possibleConnectionsButtons = new GuiButton[possibleConnections.size()];
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public void initGui() {
    buttonList.clear();
    for (int i = 0; i < possibleConnections.size(); i++) {
      possibleConnectionsButtons[i] = new GuiButton(i, width/2 - 200/2, i*20, 200, 20, possibleConnections.get(i).toString());
      buttonList.add(possibleConnectionsButtons[i]);
    }
    super.initGui();
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    OpticalFiberConnection connection;
    if (true) {
      connection = new OpticalFiberInput(this.pos, this.possibleConnections.get(button.id).getDirection(), OpticalFiberConnection.ConnectionType.ITEMS);
    } else {
      connection = new OpticalFiberOutput(this.pos, this.possibleConnections.get(button.id).getDirection(), OpticalFiberConnection.ConnectionType.ITEMS);
    }
    FiberOpticsPacketHandler.INSTANCE.sendToServer(new OpticalFiberConnectionCreationMessage(connection));
    super.actionPerformed(button);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
