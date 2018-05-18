package com.jsorrell.fiberoptics.client.gui;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.OpticalFiberConnectionCreationRequest;
import com.jsorrell.fiberoptics.transfer_type.TransferType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumFacing;

import java.io.IOException;
import java.util.List;

public class TerminatorGui extends GuiScreen {
  private TileOpticalFiberBase tile;
  private List<TileOpticalFiberBase.PossibleConnection> possibleConnections;
  private IGuiPage currentPage;

  // Build Connection
  private TransferType transferType;
  private EnumFacing connectionFacing;

  public TerminatorGui(TileOpticalFiberBase tile) {
    this.tile = tile;
    currentPage = new MainPage();
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

  private List<TileOpticalFiberBase.PossibleConnection> getPossibleConnections() {
    if (possibleConnections == null) {
      possibleConnections = tile.getPossibleConnections();
    }
    return possibleConnections;
  }

  /*
   * Main Page
   */
  private class MainPage implements IGuiPage {
    private List<TileOpticalFiberBase.PossibleConnection> possibleConnections;
    private GuiButton[] possibleConnectionsButtons;
    public MainPage() {
      this.possibleConnections = getPossibleConnections();
      this.possibleConnectionsButtons = new GuiButton[possibleConnections.size()];
    }

    @Override
    public void initGui() {
      buttonList.clear();
      for (int i = 0; i < this.possibleConnections.size(); i++) {
        TileOpticalFiberBase.PossibleConnection possibleConnection = this.possibleConnections.get(i);
        this.possibleConnectionsButtons[i] = new GuiButton(i, width/2 - 200/2, i*20, 200, 20, possibleConnection.getFacing().toString() + " " + possibleConnection.getTransferType().getName());
        buttonList.add(possibleConnectionsButtons[i]);
      }
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
      TileOpticalFiberBase.PossibleConnection possibleConnection = this.possibleConnections.get(button.id);
      connectionFacing = possibleConnection.getFacing();
      transferType = possibleConnection.getTransferType();
      currentPage = new DirectionPage();
      currentPage.initGui();
    }
  }

  /*
   * Direction Page
   */
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
        connection = new OpticalFiberInput(tile.getPos(), connectionFacing, transferType);
      } else {
        connection = new OpticalFiberOutput(tile.getPos(), connectionFacing, transferType);
      }

      FiberOpticsPacketHandler.INSTANCE.sendToServer(new OpticalFiberConnectionCreationRequest(connection));
      // close screen
      mc.displayGuiScreen(null);
    }
  }
}
