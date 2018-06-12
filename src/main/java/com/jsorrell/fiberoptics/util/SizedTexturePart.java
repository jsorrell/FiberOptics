package com.jsorrell.fiberoptics.util;

import jline.internal.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Dimension;

@SideOnly(Side.CLIENT)
public class SizedTexturePart extends TexturePart {
  public final Dimension size;

  public SizedTexturePart(ResourceLocation texture, Dimension size) {
    super(texture);
    Preconditions.checkNotNull(size);
    this.size = size;
  }

  public SizedTexturePart(ResourceLocation texture, TextureOffset offset, Dimension size) {
    super(texture, offset);
    Preconditions.checkNotNull(size);
    this.size = size;
  }

  public SizedTexturePart(TexturePart part, Dimension size) {
    this(part.texture, part.offset, size);
  }

  public void drawTexturePart(Minecraft mc, int x, int y, float zLevel) {
    Preconditions.checkNotNull(mc);
    mc.renderEngine.bindTexture(this.texture);
    GuiUtils.drawTexturedModalRect(x, y, offset.textureOffsetX, offset.textureOffsetY, size.getWidth(), size.getHeight(), zLevel);
  }
}
