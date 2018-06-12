package com.jsorrell.fiberoptics.client.gui.components;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public interface IGuiListElement {
//  protected static final SizedTexturePart BUTTON_TEXTURE = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new Dimension(157, 23));
//
//  protected final int id;
//
//  protected int x, y;
//
//  public GuiListElement(int id) {
//    this.id = id;
//  }

  default List<GuiButton> initListElement(int elementNum, int x, int y) {
    return ImmutableList.of();
  }
//    this.x = x;
//    this.y = y;
//    return new ArrayList<>();
//  }

  default void drawListElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {}
//    GlStateManager.color(1F, 1F, 1F, 1F);
//    BUTTON_TEXTURE.drawTexturePart(mc, x, y, this.zLevel);
//  }
}
