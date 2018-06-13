package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import com.jsorrell.fiberoptics.message.optical_fiber.SerializeUtils;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
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
    SerializeUtils.writeUTF8String(buf, TransferType.getKeyFromType(this.getTransferType()).toString());
    SerializeUtils.writeUTF8String(buf, this.getTransferType().getKeyFromConnection(this.getClass()).toString());
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
    Class<? extends OpticalFiberConnection> connectionType = type.getConnectionFromKey(new ResourceLocation(SerializeUtils.readUTF8String(buf)));
    try {
      Constructor<? extends OpticalFiberConnection> constructor = connectionType.getConstructor(ByteBuf.class);
      return constructor.newInstance(buf);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Connections must have a public constructor that takes a ByteBuf.");
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
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
    Class<? extends  OpticalFiberConnection> connectionClass;
    try {
      TransferType<?> transferType = TransferType.getTypeFromKey(new ResourceLocation(compound.getString("_TransferType")));
      connectionClass = transferType.getConnectionFromKey(new ResourceLocation(compound.getString("_ConnectionType")));
    } catch (TransferType.NoTypeForKeyException e) {
      throw new InvalidTypeKeyException(e.key);
    } catch (TransferType.NoConnectionForKeyException e) {
      throw new InvalidConnectionKeyException(e.typeKey, e.connectionKey);
    }

    try {
      Constructor<? extends OpticalFiberConnection> constructor = connectionClass.getConstructor(NBTTagCompound.class);
      return constructor.newInstance(compound);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(connectionClass.getName() + " must have a public constructor that takes a NBTTagCompound.");
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
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
    compound.setString("_TransferType", TransferType.getKeyFromType(this.getTransferType()).toString());
    compound.setString("_ConnectionType", this.getTransferType().getKeyFromConnection(this.getClass()).toString());
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


  /* Define Connection */

  /**
   * Returns the transfer type associated with the connection.
   * @return the transfer type associated with the connection.
   */
  public abstract TransferType getTransferType();

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
    TransferType type = this.getTransferType();
    if ((cmp = type.getKeyFromConnection(this.getClass()).compareTo(type.getKeyFromConnection(connection.getClass()))) != 0) return cmp;
    return 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pos, side, channelName);
  }
}