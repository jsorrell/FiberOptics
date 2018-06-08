package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.vecmath.Point2d;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;

public class GuiTypeChooser extends GuiConnectionBuilder {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));

  private static final Point2d[][] BUTTON_POSITIONS = {
          {new Point2d(1/2D, 1/2D)}, // 1
          {new Point2d(1/3D, 1/2D), new Point2d(1D/3D, 1D/2D)}, //2
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D)}, //3
          {new Point2d(1/2D, 2/3D - Math.sqrt(3)/4), new Point2d(1/4D, 2/3D), new Point2d(3/4D, 2/3D), new Point2d(1/2D, 1/2D)}, //4
          {new Point2d(1/4D, 1/4D), new Point2d(3/4D, 1/4D), new Point2d(1/2D, 1/2D), new Point2d(1/4D, 3/4D), new Point2d(3/4D, 3/4D)}, //5
  };

  private static final int[] BUTTON_SIZES = { 96, 40, 32, 32, 28 };

  private final ImmutableList<TransferType> types;

  public GuiTypeChooser(OpticalFiberConnectionFactory connectionFactory, Collection<TransferType> types) {
    super(connectionFactory);
    this.types = ImmutableList.copyOf(types);
  }

  public GuiTypeChooser(BlockPos pos, EnumFacing side, Collection<TransferType> types) {
    this(new OpticalFiberConnectionFactory(pos, side), types);
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  @Override
  public void initGui() {
    super.initGui();
    int size = BUTTON_SIZES[this.types.size()-1];
    for (int i = 0; i < this.types.size(); ++i) {
      Point2d pos = BUTTON_POSITIONS[this.types.size()-1][i];
      int x = this.backgroundStart.x + (int)Math.round(pos.x * BACKGROUND.size.width - size/2D);
      int y = this.backgroundStart.y + (int)Math.round(pos.y * BACKGROUND.size.height - size/2D);
      GuiTypeButton button = new GuiTypeButton(i, x, y, new Dimension(size, size), this.types.get(i));
      this.buttonList.add(button);
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    for (GuiButton button : this.buttonList) {
      if (button instanceof GuiTypeButton) {
        ((GuiTypeButton) button).drawTooltip(this.mc, mouseX, mouseY, partialTicks);
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (0 <= button.id && button.id < this.types.size()) {
      this.connectionFactory.setTransferType(this.types.get(button.id));
      mc.displayGuiScreen(new GuiDirectionChooser(this.connectionFactory));
    }
    super.actionPerformed(button);
  }
}
