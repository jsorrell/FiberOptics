package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiTypeChooser extends GuiConnectionBuilder {
  private static final int BUTTON_WIDTH = 200;
  private static final int BUTTON_HEIGHT = 20;

  public GuiTypeChooser(OpticalFiberConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  public GuiTypeChooser(BlockPos pos, EnumFacing side) {
    this(new OpticalFiberConnectionFactory(pos, side));
 }

  @Override
  public void initGui() {
    for (int i = 0; i < ModTransferTypes.VALUES.length; ++i) {
      int yValue = (this.height-BUTTON_HEIGHT*ModTransferTypes.VALUES.length)/2 + i*BUTTON_HEIGHT;
      GuiButton button = new GuiButton(i, (this.width - BUTTON_WIDTH)/2, yValue, BUTTON_WIDTH, BUTTON_HEIGHT, ModTransferTypes.fromIndex(i).getName());
      this.buttonList.add(button);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (0 <= button.id && button.id < ModTransferTypes.VALUES.length) {
      this.connectionFactory.setTransferType(ModTransferTypes.fromIndex(button.id));
      mc.displayGuiScreen(new GuiDirectionChooser(this.connectionFactory));
    }
    super.actionPerformed(button);
  }
}
