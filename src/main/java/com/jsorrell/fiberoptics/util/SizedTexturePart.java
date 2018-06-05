package com.jsorrell.fiberoptics.util;

import jline.internal.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SizedTexturePart extends TexturePart {
  public final TextureSize size;

  public SizedTexturePart(ResourceLocation texture, TextureSize size) {
    super(texture);
    Preconditions.checkNotNull(size);
    this.size = size;
  }

  public SizedTexturePart(ResourceLocation texture, TextureOffset offset, TextureSize size) {
    super(texture, offset);
    Preconditions.checkNotNull(size);
    this.size = size;
  }

  public SizedTexturePart(TexturePart part, TextureSize size) {
    this(part.texture, part.offset, size);
  }

  public void drawTexturePart(Minecraft mc, Gui gui, int x, int y) {
    Preconditions.checkNotNull(mc);
    mc.renderEngine.bindTexture(this.texture);
    gui.drawTexturedModalRect(x, y, offset.textureOffsetX, offset.textureOffsetY, this.size.width, this.size.height);
  }
}
