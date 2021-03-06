package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Point;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class GuiOpticalFiber extends GuiScreen {
  protected static final SizedTexturePart EMPTY_BACKGROUND;

  static {
    ResourceLocation bgLocation = new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png");
    TextureOffset offset = new TextureOffset(0, 0);
    Dimension size = new Dimension(206, 195);

    EMPTY_BACKGROUND = new SizedTexturePart(bgLocation, offset, size);
  }

  protected Point backgroundStart;

  @Nullable
  public SizedTexturePart getBackgroundTexture() {
    return EMPTY_BACKGROUND;
  }

  @Override
  public void initGui() {
    SizedTexturePart background;
    if ((background = getBackgroundTexture()) != null) {
      this.backgroundStart = new Point((this.width - background.size.getWidth())/2, (this.height - background.size.getHeight())/2);
    }
  }

  @Override
  @Deprecated
  public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mc == null) return; // Is this a bug in forge? Sometimes this is called by EntityRenderer#updateCameraAndRender before GuiScreen#setWorldAndResolution
    if (this.backgroundStart == null) return; // FIXME why is this happening.
    this.drawScreenUnchecked(mouseX, mouseY, partialTicks);
  }

  protected void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    this.drawBackground();
    this.drawButtonsAndLabels(mouseX, mouseY, partialTicks);
  }

  public void drawBackground() {
    SizedTexturePart background;
    if ((background = getBackgroundTexture()) != null) {
      background.drawTexturePart(this.mc, backgroundStart.getX(), backgroundStart.getY(), this.zLevel);
    }
  }

  public void drawButtonsAndLabels(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

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
