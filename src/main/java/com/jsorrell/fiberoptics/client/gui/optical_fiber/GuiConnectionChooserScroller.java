package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.util.AADirection2D;
import com.jsorrell.fiberoptics.util.TexturePart;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiConnectionChooserScroller extends GuiListScroller {
  protected static final Dimension SIZE = new Dimension(12,15);
  protected static final TexturePart TEXTURE = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(232, 0));
  protected static final TexturePart TEXTURE_LOCKED = new TexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(244, 0));
  public GuiConnectionChooserScroller(int id, int x, int y, AADirection2D scrollDirection, int scrollHeight, int listSize) {
    super(id, x, y, scrollDirection, scrollHeight, SIZE, TEXTURE, TEXTURE_LOCKED, listSize);
  }
}
