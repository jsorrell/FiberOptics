package com.jsorrell.fiberoptics.client.gui.optical_fiber;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionFactory;
import com.jsorrell.fiberoptics.util.SizedTexturePart;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.Dimension;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GuiScreenConfig extends GuiOpticalFiber {
  protected static final SizedTexturePart BACKGROUND = new SizedTexturePart(new ResourceLocation(FiberOptics.MODID, "textures/gui/empty_background.png"), new TextureOffset(0, 0), new Dimension(206, 195));

  protected OpticalFiberConnectionFactory connectionFactory;

  public GuiScreenConfig(BlockPos pos, EnumFacing side) {
  }

  public GuiScreenConfig(OpticalFiberConnection connection) {
//    this.connectionFactory = new OpticalFiberConnectionFactory(connection);
  }

  @Nullable
  @Override
  public SizedTexturePart getBackgroundTexture() {
    return BACKGROUND;
  }

  protected final void onBack() {

  }

  protected final void onCancel() {

  }

  protected final void onSubmit(OpticalFiberConnection connection) {

  }
}
