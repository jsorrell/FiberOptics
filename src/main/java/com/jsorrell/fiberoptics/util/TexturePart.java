package com.jsorrell.fiberoptics.util;

import jline.internal.Preconditions;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturePart {
  public final ResourceLocation texture;
  public final TextureOffset offset;

  public TexturePart(ResourceLocation texture) {
    this(texture, new TextureOffset(0, 0));
  }

  public TexturePart(ResourceLocation texture, TextureOffset offset) {
    Preconditions.checkNotNull(texture);
    Preconditions.checkNotNull(offset);
    this.texture = texture;
    this.offset = offset;
  }
}
