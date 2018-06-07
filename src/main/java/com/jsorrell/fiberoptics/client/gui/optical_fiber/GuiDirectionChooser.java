package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.*;

public class GuiDirectionChooser extends GuiConnectionBuilder {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));
  private static final int BUTTON_WIDTH = 200;
  private static final int BUTTON_HEIGHT = 20;
  private static final int INPUT_BUTTON_ID = 0;
  private static final int OUTPUT_BUTTON_ID = 1;

  public GuiDirectionChooser(OpticalFiberConnectionFactory connectionFactory) {
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
    GuiButton inputButton = new GuiButton(INPUT_BUTTON_ID, (this.width-BUTTON_WIDTH)/2, (this.height-BUTTON_HEIGHT*2)/2, BUTTON_WIDTH, BUTTON_HEIGHT, "Input");
    GuiButton outputButton = new GuiButton(OUTPUT_BUTTON_ID, (this.width-BUTTON_WIDTH)/2, (this.height-BUTTON_HEIGHT*2)/2 + BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT, "Output");
    this.buttonList.add(inputButton);
    this.buttonList.add(outputButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    switch (button.id) {
      case INPUT_BUTTON_ID: {
        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.INPUT);
        break;
      }
      case OUTPUT_BUTTON_ID: {
        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.OUTPUT);
        break;
      }
    }

    this.mc.displayGuiScreen(new GuiConnectionCreator(this.connectionFactory));
  }
}
