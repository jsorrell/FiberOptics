package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import net.minecraft.client.gui.GuiButton;

public class GuiDirectionChooser extends GuiConnectionBuilder {
  private static final int BUTTON_WIDTH = 200;
  private static final int BUTTON_HEIGHT = 20;
  private static final int INPUT_BUTTON_ID = 0;
  private static final int OUTPUT_BUTTON_ID = 1;

  public GuiDirectionChooser(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  @Override
  public void initGui() {
    GuiButton inputButton = new GuiButton(INPUT_BUTTON_ID, (this.width-BUTTON_WIDTH)/2, (this.height-BUTTON_HEIGHT*2)/2, BUTTON_WIDTH, BUTTON_HEIGHT, "Input");
    GuiButton outputButton = new GuiButton(OUTPUT_BUTTON_ID, (this.width-BUTTON_WIDTH)/2, (this.height-BUTTON_HEIGHT*2)/2 + BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Output");
    this.buttonList.add(inputButton);
    this.buttonList.add(outputButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    switch (button.id) {
      case INPUT_BUTTON_ID: {
        System.out.println("Setting input");
        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.INPUT);
        break;
      }
      case OUTPUT_BUTTON_ID: {
        System.out.println("Setting output");
        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.OUTPUT);
        break;
      }
    }

    this.mc.displayGuiScreen(new GuiConnectionCreator(this.connectionFactory));
  }
}
