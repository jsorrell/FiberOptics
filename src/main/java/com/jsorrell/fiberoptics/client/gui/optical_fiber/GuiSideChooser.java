package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketSetSide;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GuiSideChooser extends GuiOpticalFiber {
  private static final int SIDE_BUTTON_HEIGHT = 20;
  private static final int SIDE_BUTTON_WIDTH = 200;
  public final List<GuiButton> sideButtons = new ArrayList<>(7);

  public GuiSideChooser(BlockPos pos) {
    super(pos);
  }

  // TODO maybe do this ender io style of showing connected stuff rather than having to choose North or East or whatever.

  @Override
  public void initGui() {
    int buttonYStart = (height-TEXTURE_HEIGHT)/2 + (TEXTURE_HEIGHT-7*SIDE_BUTTON_HEIGHT)/2;
    for (int i = 0; i < 6; ++i) {
      String buttonText = EnumFacing.getFront(i).getName();
      this.sideButtons.add(new GuiButton(i, width/2 - SIDE_BUTTON_WIDTH/2, buttonYStart + i*SIDE_BUTTON_HEIGHT, SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT, buttonText));
    }
    this.sideButtons.add(new GuiButton(6, width/2 - SIDE_BUTTON_WIDTH/2, buttonYStart + 6*SIDE_BUTTON_HEIGHT, SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT, "View All Connections"));
    this.buttonList.addAll(this.sideButtons);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (0 <= button.id && button.id < 6) {
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketSetSide(this.pos, EnumFacing.getFront(button.id)));
    } else {
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketSetSide(this.pos, null));
    }
  }
}
