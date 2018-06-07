package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
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
  private static final int BUTTON_WIDTH = 200;
  private static final int BUTTON_HEIGHT = 20;

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
    for (int i = 0; i < ModTransferTypes.VALUES.length; ++i) {
      int yValue = (this.height-BUTTON_HEIGHT*ModTransferTypes.VALUES.length)/2 + i*BUTTON_HEIGHT;
      GuiButton button = new GuiButton(i, (this.width - BUTTON_WIDTH)/2, yValue, BUTTON_WIDTH, BUTTON_HEIGHT, ModTransferTypes.fromIndex(i).getName());
      this.buttonList.add(button);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (0 <= button.id && button.id < ModTransferTypes.VALUES.length) {
      this.connectionFactory.setTransferType(ModTransferTypes.fromIndex(button.id));
      mc.displayGuiScreen(new GuiDirectionChooser(this.connectionFactory));
    }
    super.actionPerformed(button);
  }
}
