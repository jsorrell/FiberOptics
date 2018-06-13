package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.message.optical_fiber.SerializeUtils;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@Immutable
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class OpticalFiberConnection implements Comparable<OpticalFiberConnection> {
  public final BlockPos pos;
  public final EnumFacing side;
  public final String channelName;

  public OpticalFiberConnection(BlockPos pos, EnumFacing side, String channelName) {
    this.pos = pos.toImmutable();
    this.side = side;
    this.channelName = channelName;
  }


  /* Bytes */

  /**
   * Serialize connection to bytes.
   * @param buf the {@code ByteBuf} to write to.
   */
  public final void toBytes(ByteBuf buf) {
    SerializeUtils.writeUTF8String(buf, this.getTransferType().getRegistryKey().toString());
    SerializeUtils.writeUTF8String(buf, this.getConnectionType().getRegistryKey().toString());
    buf.writeLong(this.pos.toLong());
    buf.writeByte(this.side.getIndex());
    SerializeUtils.writeUTF8String(buf, this.channelName);

    this.writeConnectionSpecificBytes(buf);
  }

  /**
   * Read a connection from bytes.
   * @param buf the {@code ByteBuf} to read from.
   * @return the connection read.
   */
  public static OpticalFiberConnection fromBytes(ByteBuf buf) {
    TransferType<?> type = TransferType.getTypeFromKey(new ResourceLocation(SerializeUtils.readUTF8String(buf)));
    OpticalFiberConnectionType connectionType = type.getConnectionFromKey(new ResourceLocation(SerializeUtils.readUTF8String(buf)));
    return connectionType.fromBuf(buf);
  }

  /**
   * Reads and creates an OpticalFiberConnection from a {@link ByteBuf}.
   * @param buf the buf to read from.
   */
  public OpticalFiberConnection(ByteBuf buf) {
    this(BlockPos.fromLong(buf.readLong()), EnumFacing.getFront(buf.readByte()), ByteBufUtils.readUTF8String(buf));
  }


  /* NBT */

  public static class InvalidTypeKeyException extends Exception {
    public InvalidTypeKeyException(ResourceLocation key) {
      super("Invalid Transfer type \"" + key.toString() + "\".");
    }
  }

  public static class InvalidConnectionKeyException extends Exception {
    public final ResourceLocation typeKey;
    public final ResourceLocation connectionKey;
    public InvalidConnectionKeyException(ResourceLocation typeKey, ResourceLocation connectionKey) {
      super("Invalid Connection Type \"" + connectionKey.toString() + "\" for Transfer Type \"" + typeKey.toString() + "\".");
      this.typeKey = typeKey;
      this.connectionKey = connectionKey;
    }
  }

  /**
   * Helper function to read {@code pos} from a NBTTagCompound.
   * @param compound the NBTTagCompound.
   * @return the position read.
   */
  private static BlockPos readNBTPos(NBTTagCompound compound) {
    NBTTagCompound posNBT = compound.getCompoundTag("_Pos");
    return new BlockPos(posNBT.getInteger("x"), posNBT.getInteger("y"), posNBT.getInteger("z"));
  }

  /**
   * Read a connection from a {@link NBTTagCompound}.
   * @param compound the {@link NBTTagCompound} to read from.
   * @return the connection read.
   * @throws InvalidTypeKeyException thrown if the saved transfer type in the compound is invalid; occurs organically when reading a transfer type that was previously loaded but not now.
   * @throws InvalidConnectionKeyException thrown if the saved connection type in the compound is invalid; occurs organically when reading a connection type that was previously loaded but not now.
   */
  public static OpticalFiberConnection fromNBT(NBTTagCompound compound) throws InvalidTypeKeyException, InvalidConnectionKeyException {
    OpticalFiberConnectionType connectionType;
    try {
      TransferType<?> transferType = TransferType.getTypeFromKey(new ResourceLocation(compound.getString("_TransferType")));
      connectionType = transferType.getConnectionFromKey(new ResourceLocation(compound.getString("_ConnectionType")));
    } catch (TransferType.NoTypeForKeyException e) {
      throw new InvalidTypeKeyException(e.key);
    } catch (TransferType.NoConnectionForKeyException e) {
      throw new InvalidConnectionKeyException(e.typeKey, e.connectionKey);
    }

    return connectionType.fromNBT(compound);
  }

  /**
   * Creates a connection from a {@link NBTTagCompound}.
   * @param compound the compound.
   */
  public OpticalFiberConnection(NBTTagCompound compound) {
    this(readNBTPos(compound), EnumFacing.getFront(compound.getInteger("_Side")), compound.getString("_ChannelName"));
  }

  /**
   * Serialize a connection to a {@link NBTTagCompound}.
   * @return the serialized connection.
   */
  public final NBTTagCompound serializeNBT() {
    NBTTagCompound compound = new NBTTagCompound();
    compound.setString("_TransferType", this.getTransferType().getRegistryKey().toString());
    compound.setString("_ConnectionType", this.getConnectionType().getRegistryKey().toString());
    NBTTagCompound pos = new NBTTagCompound();
    compound.setTag("_Pos", pos);
    pos.setInteger("x", this.pos.getX());
    pos.setInteger("y", this.pos.getY());
    pos.setInteger("z", this.pos.getZ());
    compound.setInteger("_Side", this.side.getIndex());
    compound.setString("_ChannelName", this.channelName);

    NBTTagCompound connectionSpecificNBT = this.serializeConnectionSpecificNBT();
    if (connectionSpecificNBT != null) {
      assert !connectionSpecificNBT.hasKey("_TransferType");
      assert !connectionSpecificNBT.hasKey("_ConnectionType");
      assert !connectionSpecificNBT.hasKey("_Pos");
      assert !connectionSpecificNBT.hasKey("_Side");
      assert !connectionSpecificNBT.hasKey("_ChannelName");
      for (String key : connectionSpecificNBT.getKeySet()) {
        compound.setTag(key, connectionSpecificNBT.getTag(key));
      }
    }

    return compound;
  }


  /* Useful Functions */

  /**
   * Finds the tile which the connection is interfacing with.
   * @param worldIn the world.
   * @return Null if the connection is not connected to a tile and the tile it is connected to otherwise.
   */
  @Nullable
  public TileEntity getConnectedTile(IBlockAccess worldIn) {
    BlockPos connectedTilePos = this.pos.offset(this.side);
    return worldIn.getTileEntity(connectedTilePos);
  }

//  public static void drawConnectionTypeIcon(Class<? extends OpticalFiberConnection> clazz, Minecraft mc, float zLevel, float partialTicks) {
//    Method method;
//    try {
//      method = clazz.getMethod("drawConnectionTypeIcon", Minecraft.class, float.class, float.class);
//    } catch (NoSuchMethodException e) {
//      throw new AssertionError("Should never get here.", e.getCause());
//    }
//
//    // Not hidden
//    if (method.getDeclaringClass().equals(OpticalFiberConnection.class)) {
//      throw new RuntimeException(method.getName() + " should be hidden by " + clazz.getName() + ".");
//    }
//
//    try {
//      method.invoke(null, mc, zLevel, partialTicks);
//    } catch (IllegalAccessException e) {
//      throw new AssertionError(method.getName() + " in " + method.getDeclaringClass().getName() + " should be public.", e);
//    } catch (InvocationTargetException e) {
//      if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
//      else throw new RuntimeException(method.getName() + " in " + method.getDeclaringClass().getName() + " should not throw.", e.getCause());
//    }
//  }
//
//  public static GuiScreen getEditConnectionGui(Class<? extends OpticalFiberConnection> clazz, OpticalFiberConnection connection, Consumer<OpticalFiberConnection> handleSubmit, Runnable handleCancel, Runnable handleBack) {
//    Method method;
//    try {
//      method = clazz.getMethod("getEditConnectionGui", OpticalFiberConnection.class, Consumer.class, Runnable.class, Runnable.class);
//    } catch (NoSuchMethodException e) {
//      throw new AssertionError("Should never get here.", e);
//    }
//
//    try {
//      return (GuiScreen) method.invoke(null, connection, handleSubmit, handleCancel, handleBack);
//    } catch (IllegalAccessException e) {
//      throw new AssertionError(method.getName() + " in " + method.getDeclaringClass().getName() + " should be public.", e);
//    } catch (InvocationTargetException e) {
//      if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
//      else throw new RuntimeException(method.getName() + " in " + method.getDeclaringClass().getName() + " should not throw.", e.getCause());
//    }
//  }
//
//  public static GuiScreen getCreateConnectionGui(Class<? extends OpticalFiberConnection> clazz, BlockPos pos, EnumFacing side, String channelName, Consumer<OpticalFiberConnection> handleSubmit, Runnable handleCancel, Runnable handleBack) {
//    Method method;
//    try {
//      method = clazz.getMethod("getCreateConnectionGui", BlockPos.class, EnumFacing.class, String.class, Consumer.class, Runnable.class, Runnable.class);
//    } catch (NoSuchMethodException e) {
//      throw new AssertionError("Should never get here.", e);
//    }
//
//    try {
//      return (GuiScreen) method.invoke(null, pos, side, channelName, handleSubmit, handleCancel, handleBack);
//    } catch (IllegalAccessException e) {
//      throw new AssertionError(method.getName() + " in " + method.getDeclaringClass().getName() + " should be public.", e);
//    } catch (InvocationTargetException e) {
//      if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
//      else throw new RuntimeException(method.getName() + " in " + method.getDeclaringClass().getName() + " should not throw.", e.getCause());
//    }
//  }


  /* Define Connection */

  /**
   * Serializes the information specific to the type of connection to a {@link NBTTagCompound}.
   * @return the serialized {@link NBTTagCompound} or null.
   */
  @Nullable
  protected NBTTagCompound serializeConnectionSpecificNBT() {
    return null;
  }

  /**
   * Writes the connection specific information.
   * @param buf the buf to write to.
   */
  protected void writeConnectionSpecificBytes(ByteBuf buf) { }

  /**
   * Get whether the connection can be edited. If true {@link } must not return null for this connection type.
   * @return {@code true} iff the connection can be edited.
   */
  public boolean canEditConnection() {
    return false;
  }

  public final TransferType getTransferType() {
    return this.getConnectionType().getTransferType();
  }

  public abstract OpticalFiberConnectionType getConnectionType();

  @Override
  @OverridingMethodsMustInvokeSuper
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OpticalFiberConnection)) return false;
    OpticalFiberConnection that = (OpticalFiberConnection) o;
    return Objects.equals(pos, that.pos) &&
            side == that.side &&
            Objects.equals(channelName, that.channelName);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public int compareTo(OpticalFiberConnection connection) {
    int cmp;
    if ((cmp = this.pos.compareTo(connection.pos)) != 0) return cmp;
    if ((cmp = this.side.compareTo(connection.side)) != 0) return cmp;
    if ((cmp = this.channelName.compareTo(connection.channelName)) != 0) return cmp;
    if ((cmp = this.getTransferType().compareTo(connection.getTransferType())) != 0) return cmp;
    if ((cmp = this.getConnectionType().compareTo(connection.getConnectionType())) != 0) return cmp;
    return 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pos, side, channelName);
  }
}