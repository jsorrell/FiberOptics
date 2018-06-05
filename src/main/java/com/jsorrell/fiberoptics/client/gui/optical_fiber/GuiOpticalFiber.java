package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public abstract class GuiOpticalFiber extends GuiScreen {
//  protected final ResourceLocation TEXTURE = new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png");
//  protected static final int TEXTURE_WIDTH = 206;
//  protected static final int TEXTURE_HEIGHT = 195;
  protected final BlockPos pos;

  public GuiOpticalFiber(BlockPos pos) {
    this.pos = pos.toImmutable();
  }

//  @Override
//  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//    assert Minecraft.getMinecraft().renderEngine != null;
//    assert mc != null;
//    assert mc.renderEngine != null;
//    mc.renderEngine.bindTexture(TEXTURE);
//    drawTexturedModalRect((width-TEXTURE_WIDTH)/2, (height-TEXTURE_HEIGHT)/2, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
//    super.drawScreen(mouseX, mouseY, partialTicks);
//  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
    {
      this.mc.displayGuiScreen(null);
    }
    super.keyTyped(typedChar, keyCode);
  }
}
