package com.jsorrell.fiberoptics.client.gui;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.OpticalFiberConnectionCreationMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TerminatorGui extends GuiScreen {
  private BlockPos pos;
  private List<TileOpticalFiberBase.PossibleConnection> possibleConnections; //TODO Query server for this when main page opened
  private IGuiPage currentPage;

  // Build Connection
  private OpticalFiberConnection.ConnectionType connectionType;
  private EnumFacing connectionFacing;

  public TerminatorGui(BlockPos pos, List<TileOpticalFiberBase.PossibleConnection> possibleConnections) {
    this.pos = pos;
    this.possibleConnections = possibleConnections;
    currentPage = new MainPage(new ArrayList<>(possibleConnections));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public void initGui() {
    currentPage.initGui();
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    currentPage.actionPerformed(button);
    super.actionPerformed(button);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  private interface IGuiPage {
    void initGui();
    void actionPerformed(GuiButton button) throws IOException;
  }

  private class MainPage implements IGuiPage {
    private List<TileOpticalFiberBase.PossibleConnection> possibleConnections;
    private GuiButton[] possibleConnectionsButtons;
    public MainPage(List<TileOpticalFiberBase.PossibleConnection> possibleConnections) {
      this.possibleConnections = possibleConnections;
      this.possibleConnectionsButtons = new GuiButton[possibleConnections.size()];
    }

    @Override
    public void initGui() {
      buttonList.clear();
      for (int i = 0; i < this.possibleConnections.size(); i++) {
        this.possibleConnectionsButtons[i] = new GuiButton(i, width/2 - 200/2, i*20, 200, 20, possibleConnections.get(i).toString());
        buttonList.add(possibleConnectionsButtons[i]);
      }
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
      TileOpticalFiberBase.PossibleConnection possibleConnection = this.possibleConnections.get(button.id);
      connectionFacing = possibleConnection.getFacing();
      connectionType = possibleConnection.getConnectionType();
      currentPage = new DirectionPage();
      currentPage.initGui();
    }
  }

  public class DirectionPage implements IGuiPage {
    private GuiButton inputButton;
    private GuiButton outputButton;
    private final int INPUT_BUTTON = 0;
    private final int OUTPUT_BUTTON = 1;

    @Override
    public void initGui() {
      buttonList.clear();
      buttonList.add(inputButton = new GuiButton(INPUT_BUTTON, width/2 - 200/2, 0, 200, 20, "input"));
      buttonList.add(outputButton = new GuiButton(OUTPUT_BUTTON, width/2 - 200/2, 20, 200, 20, "output"));
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
      OpticalFiberConnection connection;
      if (button.id == INPUT_BUTTON) {
        connection = new OpticalFiberInput(pos, connectionFacing, connectionType);
      } else {
        connection = new OpticalFiberOutput(pos, connectionFacing, connectionType);
      }

      FiberOpticsPacketHandler.INSTANCE.sendToServer(new OpticalFiberConnectionCreationMessage(connection));
      // close screen
      mc.displayGuiScreen(null);
    }
  }
}
