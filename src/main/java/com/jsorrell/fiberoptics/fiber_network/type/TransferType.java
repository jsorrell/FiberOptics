package com.jsorrell.fiberoptics.fiber_network.type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnectionType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Dimension;

import javax.annotation.ParametersAreNonnullByDefault;

//TODO improve this api

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TransferType<T> implements Comparable<TransferType> {
  public static final Dimension ICON_RENDER_SIZE = new Dimension(16, 16);
  private static final BiMap<ResourceLocation, TransferType> REGISTERED_TYPES = HashBiMap.create();

  private final BiMap<ResourceLocation, OpticalFiberConnectionType> registeredConnections = HashBiMap.create();

  private final ResourceLocation registryKey;

  public TransferType(ResourceLocation registryKey) {
    this.registryKey = registryKey;
  }

  /** Registry **/
  public static void registerType(TransferType type) {
    type.registerConnections();
    REGISTERED_TYPES.put(type.getRegistryKey(), type);
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

//  public static ResourceLocation getKeyFromType(TransferType type) {
//    ResourceLocation key = REGISTERED_TYPES.inverse().get(type);
//    if (key == null) throw new RuntimeException("Transfer type " + type.getClass().getName() + " not registered.");
//    return key;
//  }


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

  public final void registerConnection(OpticalFiberConnectionType connectionType) {
    this.registeredConnections.put(connectionType.getRegistryKey(), connectionType);
  }

  public ImmutableList<? extends OpticalFiberConnectionType> getRegisteredConnections() {
    return ImmutableList.copyOf(this.registeredConnections.values());
  }

  public int getNumRegisteredConnections() {
    return this.registeredConnections.size();
  }

  public final boolean connectionIsRegistered(OpticalFiberConnectionType connectionType) {
    return this.registeredConnections.containsValue(connectionType);
  }

//  public final ResourceLocation getKeyFromConnection(OpticalFiberConnectionType connectionType) {
//    ResourceLocation key = this.registeredConnections.inverse().get(connectionType);
//    if (key == null) throw new RuntimeException("Connection type " + connectionType.getConnectionClass().getName() + " not registered.");
//    return key;
//  }

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

  public final OpticalFiberConnectionType getConnectionFromKey(ResourceLocation key) throws NoConnectionForKeyException {
    OpticalFiberConnectionType connectionType = this.registeredConnections.get(key);
    if (connectionType == null) throw new NoConnectionForKeyException(this.registryKey, key);
    return connectionType;
  }

  /** Type Options **/
  public final ResourceLocation getRegistryKey() {
    return registryKey;
  }

  public abstract String getLocalizedName();

  public abstract Capability<?> getCapability();

  public abstract boolean isOffering(T input);

  public abstract boolean doTransfer(T input, T output);

  public boolean isSource(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public boolean isSink(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  @Override
  public String toString() {
    return this.registryKey.toString();
  }

  @Override
  public final int compareTo(TransferType transferType) {
    return this.getRegistryKey().compareTo(transferType.getRegistryKey());
  }

  @SideOnly(Side.CLIENT)
  public final void drawTransferTypeIcon(Minecraft mc, int x, int y, float zLevel, Dimension size, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale((double)size.getWidth() / (double) ICON_RENDER_SIZE.getWidth(), (double)size.getHeight() / (double) ICON_RENDER_SIZE.getHeight(), 1);
    drawTransferTypeIcon(mc, zLevel, partialTicks);
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
  public abstract void drawTransferTypeIcon(Minecraft mc, float zLevel, float partialTicks);
}