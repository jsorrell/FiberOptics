package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.util.Dimension;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

public class GuiScreenTransferTypeChooser extends GuiScreenDistributedButton {

  private final ImmutableList<TransferType> types;

  protected final BlockPos pos;
  protected final EnumFacing side;
  protected final String channel;

  public GuiScreenTransferTypeChooser(BlockPos pos, EnumFacing side, String channel, Collection<TransferType> types) {
    super(types.size());
    this.pos = pos;
    this.side = side;
    this.channel = channel;
    this.types = ImmutableList.copyOf(types);
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (0 <= button.id && button.id < this.types.size()) {
      TransferType<?> type = this.types.get(button.id);
      this.mc.displayGuiScreen(new GuiScreenConnectionTypeChooser(this.pos, this.side, this.channel, type, type.getRegisteredConnections()));
    }
    super.actionPerformed(button);
  }

  @Override
  public GuiButton createButton(int idx, int x, int y, int size) {
    return new GuiTypeButton(idx, x, y, new Dimension(size, size), this.types.get(idx));
  }

  @Override
  protected void drawTooltip(int idx, int mouseX, int mouseY, float partialTicks, int screenWidth, int screenHeight, FontRenderer fontRenderer) {
    GuiUtils.drawHoveringText(ImmutableList.of(this.types.get(idx).getLocalizedName()), mouseX, mouseY, this.width, this.height, MAX_TOOLTIP_TEXT_WIDTH, mc.fontRenderer);
  }

  /* Type Button */
  private static class GuiTypeButton extends GuiButton {
    public final TransferType type;

    public GuiTypeButton(int id, int x, int y, Dimension size, TransferType type) {
      super(id, x, y, size.getWidth(), size.getHeight(), "");
      this.type = type;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        // Icon
        this.type.drawTransferTypeIcon(mc, x, y, this.zLevel, new Dimension(this.width, this.height), partialTicks);
      }
    }
  }
}
