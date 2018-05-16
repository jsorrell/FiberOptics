package com.jsorrell.fiberoptics.client.gui;

import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberItemInput;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.OpticalFiberConnectionCreationMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class TerminatorGui extends GuiScreen {
  private BlockPos pos;
  private GuiButton testButton;
  private static final int TEST_BUTTON = 0;

  public TerminatorGui(BlockPos pos) {
    this.pos = pos;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();

    testButton.drawButton(mc, mouseX, mouseY, 0F);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public void initGui() {
    buttonList.clear();
    buttonList.add(testButton = new GuiButton(TEST_BUTTON, 0, 0, 200, 20, "TestButton"));
    super.initGui();
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    switch (button.id) {
      case(TEST_BUTTON): {
        OpticalFiberConnection connection = new OpticalFiberItemInput(this.pos, EnumFacing.UP);
        FiberOpticsPacketHandler.INSTANCE.sendToServer(new OpticalFiberConnectionCreationMessage(connection));
      }
    }
    super.actionPerformed(button);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
