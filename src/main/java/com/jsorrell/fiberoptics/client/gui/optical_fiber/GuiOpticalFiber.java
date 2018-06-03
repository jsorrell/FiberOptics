package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class GuiOpticalFiber extends GuiScreen {
  protected final ResourceLocation TEXTURE = new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png");
  protected static final int TEXTURE_WIDTH = 256;
  protected static final int TEXTURE_HEIGHT = 256;
  protected final BlockPos pos;

  public GuiOpticalFiber(BlockPos pos) {
    this.pos = pos.toImmutable();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    assert mc != null;
    assert mc.renderEngine != null;
    mc.renderEngine.bindTexture(TEXTURE);
    drawTexturedModalRect((width-TEXTURE_WIDTH)/2, (height-TEXTURE_HEIGHT)/2, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
