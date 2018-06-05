package com.jsorrell.fiberoptics.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureSize {
  public final int width;
  public final int height;

  public TextureSize(int width, int height) {
    this.width = width;
    this.height = height;
  }
}
