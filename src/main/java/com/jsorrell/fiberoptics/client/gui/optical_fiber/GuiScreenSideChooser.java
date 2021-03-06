package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.Dimension;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiScreenSideChooser extends GuiOpticalFiber {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));
  private static final int SIDE_BUTTON_HEIGHT = 20;
  private static final int SIDE_BUTTON_WIDTH = 200;
  public final List<GuiButton> sideButtons = new ArrayList<>(7);

  protected final BlockPos pos;

  public GuiScreenSideChooser(BlockPos pos) {
    this.pos = pos;
  }

  // TODO maybe do this ender io style of showing connected stuff rather than having to choose North or East or whatever.

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  @Override
  public void initGui() {
    super.initGui();

    int buttonYStart = this.backgroundStart.getY() + (BACKGROUND.size.getHeight()-7*SIDE_BUTTON_HEIGHT)/2;
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
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketOpenConnectionChooser.Request(this.pos, EnumFacing.getFront(button.id)));
    } else {
      FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketOpenConnectionChooser.Request(this.pos, null));
    }
  }
}
