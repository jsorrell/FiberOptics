package com.jsorrell.fiberoptics.fiber_network.type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Dimension;

import javax.annotation.ParametersAreNonnullByDefault;

//TODO improve this api

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TransferType<T> implements Comparable<TransferType> {
  public static final Dimension RENDER_SIZE = new Dimension(16, 16);
  private static final BiMap<ResourceLocation, TransferType> REGISTERED_TYPES = HashBiMap.create();

  private final BiMap<ResourceLocation, Class<? extends OpticalFiberConnection>> registeredConnections = HashBiMap.create();

  /** Registry **/
  public static void register(TransferType type, ResourceLocation key) {
    type.registerConnections();
    REGISTERED_TYPES.put(key, type);
  }

  public static ImmutableList<TransferType> getRegisteredTypes() {
    return ImmutableList.copyOf(REGISTERED_TYPES.values());
  }

  public static int getNumRegisteredTypes() {
    return REGISTERED_TYPES.size();
  }

  public static boolean typeIsRegistered(TransferType type) {
    return REGISTERED_TYPES.containsValue(type);
  }

  public static ResourceLocation getKeyFromType(TransferType type) {
    ResourceLocation key = REGISTERED_TYPES.inverse().get(type);
    if (key == null) throw new RuntimeException("Transfer type " + type.getClass().getName() + " not registered.");
    return key;
  }


  public static boolean hasTypeForKey(ResourceLocation key) {
    return REGISTERED_TYPES.containsKey(key);
  }

  public static class NoTypeForKeyException extends RuntimeException {
    public final ResourceLocation key;
    public NoTypeForKeyException(ResourceLocation key) {
      super("No type is registered as \"" + key.toString() + "\".");
      this.key = key;
    }
  }

  public static TransferType getTypeFromKey(ResourceLocation key) throws NoTypeForKeyException {
    TransferType type = REGISTERED_TYPES.get(key);
    if (type == null) throw new NoTypeForKeyException(key);
    return type;
  }

  public final void registerConnection(Class<? extends OpticalFiberConnection> connection, ResourceLocation key) {
    this.registeredConnections.put(key, connection);
  }

  public final boolean connectionIsRegistered(Class<? extends OpticalFiberConnection> connection) {
    return this.registeredConnections.containsValue(connection);
  }

  public final ResourceLocation getKeyFromConnection(Class<? extends OpticalFiberConnection> connection) {
    ResourceLocation key = this.registeredConnections.inverse().get(connection);
    if (key == null) throw new RuntimeException("Connection type " + connection.getName() + " not registered.");
    return key;
  }

  public final boolean hasConnectionForKey(ResourceLocation key) {
    return this.registeredConnections.containsKey(key);
  }

  public static class NoConnectionForKeyException extends RuntimeException {
    public final ResourceLocation typeKey;
    public final ResourceLocation connectionKey;
    public NoConnectionForKeyException(ResourceLocation typeKey, ResourceLocation connectionKey) {
      super("No connection is registered as \"" + connectionKey.toString() + "\" in \"" + typeKey.toString() + "\".");
      this.typeKey = typeKey;
      this.connectionKey = connectionKey;
    }
  }

  public final Class<? extends OpticalFiberConnection> getConnectionFromKey(ResourceLocation key) throws NoConnectionForKeyException {
    Class<? extends OpticalFiberConnection> connection = this.registeredConnections.get(key);
    if (connection == null) throw new NoConnectionForKeyException(TransferType.getKeyFromType(this), key);
    return connection;
  }

  /** Type Options **/
  public abstract Capability<?> getCapability();

  public abstract boolean isOffering(T input);

  public abstract boolean doTransfer(T input, T output);

  public boolean isSource(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public boolean isSink(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public abstract String getUnlocalizedName();

  public String getName() {
    return I18n.format("transferType." + getUnlocalizedName() + ".name");
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public final int compareTo(TransferType transferType) {
    if (transferType == this) return 0;
    return REGISTERED_TYPES.inverse().get(this).compareTo(REGISTERED_TYPES.inverse().get(transferType));
  }

  @SideOnly(Side.CLIENT)
  public final void drawTypeIcon(Minecraft mc, int x, int y, float zLevel, float partialTicks) {
    drawTypeIcon(mc, x, y, zLevel, RENDER_SIZE, partialTicks);
  }

  @SideOnly(Side.CLIENT)
  public final void drawTypeIcon(Minecraft mc, int x, int y, float zLevel, Dimension size, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale((double)size.getWidth() / (double)RENDER_SIZE.getWidth(), (double)size.getHeight() / (double)RENDER_SIZE.getHeight(), 1);
    drawTypeIcon(mc, zLevel, partialTicks);
    GlStateManager.popMatrix();
  }

  public abstract void registerConnections();

  @Override
  public final boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  /**
   * Render a 16x16 icon at (0,0).
   */
  @SideOnly(Side.CLIENT)
  public abstract void drawTypeIcon(Minecraft mc, float zLevel, float partialTicks);

  @SideOnly(Side.CLIENT)
  public abstract void displayCreateConnectionGui(Minecraft mc, BlockPos pos, EnumFacing side);

  @SideOnly(Side.CLIENT)
  public abstract void displayEditConnectionGui(Minecraft mc, OpticalFiberConnection connection);
}