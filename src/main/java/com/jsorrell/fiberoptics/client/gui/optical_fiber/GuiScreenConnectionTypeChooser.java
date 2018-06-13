package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionType;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketCreateConnection;
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
import java.util.List;
import java.util.function.Consumer;

public class GuiScreenConnectionTypeChooser extends GuiScreenDistributedButton {

  protected final BlockPos pos;
  protected final EnumFacing side;
  protected final String channel;
  protected final TransferType transferType;

  private final List<? extends OpticalFiberConnectionType> connectionTypes;


  public GuiScreenConnectionTypeChooser(BlockPos pos, EnumFacing side, String channel, TransferType transferType, Collection<? extends  OpticalFiberConnectionType> connectionTypes) {
    super(connectionTypes.size());
    System.out.println(connectionTypes);
    this.pos = pos;
    this.side = side;
    this.channel = channel;
    this.transferType = transferType;
    this.connectionTypes = ImmutableList.copyOf(connectionTypes);
  }

  @Override
  protected GuiButton createButton(int idx, int x, int y, int size) {
    return new GuiConnectionTypeButton(idx, x, y, new Dimension(size, size), this.connectionTypes.get(idx));
  }

  @Override
  protected void drawTooltip(int idx, int mouseX, int mouseY, float partialTicks, int screenWidth, int screenHeight, FontRenderer fontRenderer) {
    GuiUtils.drawHoveringText(ImmutableList.of(this.connectionTypes.get(idx).getShortLocalizedName()), mouseX, mouseY, this.width, this.height, MAX_TOOLTIP_TEXT_WIDTH, mc.fontRenderer);
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    if (button instanceof GuiConnectionTypeButton) {
      Consumer<OpticalFiberConnection> handleSubmit = c -> FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketCreateConnection(c));
      Runnable handleCancel = () -> this.mc.displayGuiScreen(null);
      Runnable handleBack = () -> this.mc.displayGuiScreen(this);
      this.mc.displayGuiScreen(((GuiConnectionTypeButton) button).connectionType.getCreateConnectionGui(this.pos, this.side, this.channel, handleSubmit, handleCancel, handleBack));
    }
  }

  /* Connection Type Button */
  private static class GuiConnectionTypeButton extends GuiButton {
    public final OpticalFiberConnectionType connectionType;

    public GuiConnectionTypeButton(int id, int x, int y, Dimension size, OpticalFiberConnectionType connectionType) {
      super(id, x, y, size.getWidth(), size.getHeight(), "");
      this.connectionType = connectionType;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        // Icon
        this.connectionType.drawConnectionTypeIcon(mc, this.zLevel, partialTicks);
      }
    }
  }
}
