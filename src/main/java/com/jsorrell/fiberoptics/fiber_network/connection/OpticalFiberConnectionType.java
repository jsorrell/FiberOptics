package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class OpticalFiberConnectionType implements Comparable<OpticalFiberConnectionType> {
  private final ResourceLocation registryKey;
  private final TransferType transferType;

  public OpticalFiberConnectionType(ResourceLocation registryKey, TransferType transferType) {
    this.registryKey = registryKey;
    this.transferType = transferType;
  }

  public final ResourceLocation getRegistryKey() {
    return this.registryKey;
  }

  public abstract OpticalFiberConnection fromBuf(ByteBuf buf);

  public abstract OpticalFiberConnection fromNBT(NBTTagCompound compound);

  /**
   * Returns the transfer type associated with the connection type.
   * @return the transfer type associated with the connection type.
   */
  public final TransferType getTransferType() {
    return this.transferType;
  }

  /**
   * May be type dependent e.g. 'Output'.
   */
  public abstract String getShortLocalizedName();

  /**
   * Should be type independent e.g. 'Item Output'.
   */
  public String getFullLocalizedName() {
    return this.getTransferType().getLocalizedName() + " " + this.getShortLocalizedName();
  }

  /**
   * Gets the screen to display in order to create a new connection of this type.
   * @param pos the position of the new connection.
   * @param side the side of the fiber that the connection is on.
   * @param channel the channel that the connection is on.
   * @param onSubmit the function to call if and when the new connection is submitted; pass the new connection.
   * @param onCancel the function to call if and when a cancel button is pressed.
   * @param onBack the function to call if and when a back button is pressed.
   * @return the {@link GuiScreen} to display in order to create the connection of type {@code clazz}.
   * If there is no screen, just call onSubmit on the new connection and return null.
   */
  @SideOnly(Side.CLIENT)
  @Nullable
  public abstract GuiScreen getCreateConnectionGui(BlockPos pos, EnumFacing side, String channel, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack);

  /**
   * Gets the {@link GuiScreen} to display when editing the connection. When the connection has been edited,
   * @param connectionToEdit the connection to edit.
   * @param onSubmit the function to call if and when the edit is submitted; pass the new connection.
   * @param onCancel the function to call if and when a cancel button is pressed.
   * @param onBack the function to call if and when a back button is pressed.
   * @return null if the connection type cannot be edited or a supplier for the edit screen if it can be.
   */
  @SideOnly(Side.CLIENT)
  @Nullable
  public Supplier<GuiScreen> getEditConnectionGui(OpticalFiberConnection connectionToEdit, Consumer<OpticalFiberConnection> onSubmit, Runnable onCancel, Runnable onBack) {
    return null;
  }

  public abstract void drawConnectionTypeIcon(Minecraft mc, float zLevel, float partialTicks);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OpticalFiberConnectionType)) return false;
    OpticalFiberConnectionType that = (OpticalFiberConnectionType) o;
    return this.getRegistryKey().equals(that.getRegistryKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getRegistryKey());
  }

  @Override
  public String toString() {
    return this.getRegistryKey().toString();
  }

  @Override
  public final int compareTo(OpticalFiberConnectionType connectionType) {
    return this.getRegistryKey().compareTo(connectionType.getRegistryKey());
  }
}
