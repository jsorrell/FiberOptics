package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.client.gui.components.GuiIconButton;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.awt.*;

//FIXME this is unused and broken. Use this within connections
public class GuiScreenDirectionChooser extends GuiOpticalFiber {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));
  protected static final SizedTexturePart INSERT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(17, 43), new Dimension(16, 16));
  protected static final SizedTexturePart EXTRACT_ICON = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/widgets.png"), new TextureOffset(33, 43), new Dimension(16, 16));
  protected static final Dimension BUTTON_SIZE = new Dimension(40, 40);
  private static final int EXTRACT_BUTTON_ID = 0;
  private static final int INSERT_BUTTON_ID = 1;

  public GuiScreenDirectionChooser(BlockPos pos, EnumFacing side) {
    super(pos);
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  @Override
  public void initGui() {
    super.initGui();

    /* Input */
    GuiIconButton inputButton = new GuiIconButton(EXTRACT_BUTTON_ID, (int)Math.round(this.backgroundStart.x + BACKGROUND.size.width/4D - BUTTON_SIZE.width/2D), (int)Math.round(this.backgroundStart.y + BACKGROUND.size.height/2D - BUTTON_SIZE.height/2D), BUTTON_SIZE, EXTRACT_ICON);
    inputButton.tooltip = ImmutableList.of(OpticalFiberConnection.TransferDirection.EXTRACT.getName());
    this.buttonList.add(inputButton);

    /* Output */
    GuiIconButton outputButton = new GuiIconButton(INSERT_BUTTON_ID, (int)Math.round(this.backgroundStart.x + 3*BACKGROUND.size.width/4D - BUTTON_SIZE.width/2D), (int)Math.round(this.backgroundStart.y + BACKGROUND.size.height/2D - BUTTON_SIZE.height/2D), BUTTON_SIZE, INSERT_ICON);
    outputButton.tooltip = ImmutableList.of(OpticalFiberConnection.TransferDirection.INSERT.getName());
    this.buttonList.add(outputButton);
  }

  @Override
  public void drawScreenUnchecked(int mouseX, int mouseY, float partialTicks) {
    super.drawScreenUnchecked(mouseX, mouseY, partialTicks);
    for (GuiButton button : this.buttonList) {
      if (button instanceof GuiIconButton) {
        ((GuiIconButton) button).drawTooltip(this.mc, mouseX, mouseY, partialTicks);
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    switch (button.id) {
      case EXTRACT_BUTTON_ID: {
//        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.EXTRACT);
        break;
      }
      case INSERT_BUTTON_ID: {
//        this.connectionFactory.setDirection(OpticalFiberConnection.TransferDirection.INSERT);
        break;
      }
    }

    // TODO
//    this.mc.displayGuiScreen(new GuiConnectionCreator(this.connectionFactory));
  }
}
