package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import com.jsorrell.fiberoptics.util.TextureSize;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;

public class GuiButtonConnectionAdd extends GuiButtonImage {
  protected static final SizedTexturePart TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(205, 26), new TextureSize(17, 17));
  protected static final int PRESSED_Y_OFFSET = 17;
  public final GuiSideListElement listElement;

  public GuiButtonConnectionAdd(int id, int x, int y, GuiSideListElement listElement) {
    super(id, x, y, TEXTURE.size.width, TEXTURE.size.height, TEXTURE.offset.textureOffsetX, TEXTURE.offset.textureOffsetY, PRESSED_Y_OFFSET, TEXTURE.texture);
    this.listElement = listElement;
  }
}
