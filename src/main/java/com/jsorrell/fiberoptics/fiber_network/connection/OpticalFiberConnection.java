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

  public OpticalFiberConnection(ByteBuf buf) {
    this(BlockPos.fromLong(buf.readLong()), EnumFacing.getFront(buf.readByte()), ByteBufUtils.readUTF8String(buf));
  }

  public OpticalFiberConnection(NBTTagCompound compound) {
    this(new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z")), EnumFacing.getFront(compound.getInteger("Side")), compound.getString("ChannelName"));
  }

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

  public static OpticalFiberConnection fromNBT(NBTTagCompound compound) throws InvalidTypeKeyException, InvalidConnectionKeyException {
    Class<? extends  OpticalFiberConnection> connectionClass;
    try {
      TransferType<?> transferType = TransferType.getTypeFromKey(new ResourceLocation(compound.getString("TransferType")));
      connectionClass = transferType.getConnectionFromKey(new ResourceLocation(compound.getString("ConnectionType")));
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

  public static NBTTagCompound serializeNBT(OpticalFiberConnection connection) {
    NBTTagCompound compound = new NBTTagCompound();
    compound.setString("TransferType", TransferType.getKeyFromType(connection.getTransferType()).toString());
    compound.setString("ConnectionType", connection.getTransferType().getKeyFromConnection(connection.getClass()).toString());
    NBTTagCompound pos = new NBTTagCompound();
    compound.setTag("ConnectionPos", pos);
    pos.setInteger("x", connection.pos.getX());
    pos.setInteger("y", connection.pos.getY());
    pos.setInteger("z", connection.pos.getZ());
    compound.setInteger("Side", connection.side.getIndex());
    compound.setString("ChannelName", connection.channelName);

    NBTTagCompound connectionSpecificNBT = connection.serializeConnectionSpecificNBT();
    if (connectionSpecificNBT != null) {
      compound.setTag("ConnectionSpecific", connectionSpecificNBT);
    }

    return compound;
  }

  @Nullable
  protected static NBTTagCompound getConnectionSpecificNBT(NBTTagCompound compound) {
    if (!compound.hasKey("ConnectionSpecific", Constants.NBT.TAG_COMPOUND)) return null;
    return compound.getCompoundTag("ConnectionSpecific");
  }

//  public enum TransferDirection {
//    EXTRACT("extract"), // Extract from tile entity into network
//    INSERT("insert"); // Insert into tile entity from network
//    private final String unlocalizedName;
//
//    TransferDirection(String unlocalizedName) {
//      this.unlocalizedName = unlocalizedName;
//    }
//
//    public String getUnlocalizedName() {
//      return this.unlocalizedName;
//    }
//
//    public String getName() {
//      return I18n.format("transfer_direction." + getUnlocalizedName() + ".name");
//    }
//  }

  @Nullable
  protected NBTTagCompound serializeConnectionSpecificNBT() {
    return null;
  }

  @OverridingMethodsMustInvokeSuper
  public void toBytes(ByteBuf buf) {
    SerializeUtils.writeUTF8String(buf, TransferType.getKeyFromType(this.getTransferType()).toString());
    SerializeUtils.writeUTF8String(buf, this.getTransferType().getKeyFromConnection(this.getClass()).toString());
    buf.writeLong(this.pos.toLong());
    buf.writeByte(this.side.getIndex());
    SerializeUtils.writeUTF8String(buf, this.channelName);
  }

  @Nullable
  public TileEntity getConnectedTile(IBlockAccess worldIn) {
    BlockPos connectedTilePos = this.pos.offset(this.side);
    return worldIn.getTileEntity(connectedTilePos);
  }

//  @Nullable
//  protected Object getCapabilityHandler(IBlockAccess worldIn) {
//    TileEntity connectedTile = worldIn.getTileEntity(pos.offset(this.side));
//    if (connectedTile == null) {
//      return null;
//    }
//    Capability<?> capability = this.transferType.getCapability();
//    return connectedTile.getCapability(capability, this.side.getOpposite());
//  }

  public abstract TransferType getTransferType();

//  public NBTTagCompound serializeNBT() {
//    NBTTagCompound compound = new NBTTagCompound();
//    compound.setInteger("x", this.pos.getX());
//    compound.setInteger("y", this.pos.getY());
//    compound.setInteger("z", this.pos.getZ());
//    compound.setInteger("side", this.side.getIndex());
//    compound.setString("channel", this.channelName);
//    return compound;
//  }
//

  @Override
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

//  /**
//   * Add the connection to the world.
//   * @param world the world.
//   * @return {@code true} iff the connection was successfully added.
//   */
//  public boolean initialize(IBlockAccess world) {
//    return Util.getTileChecked(world, this.pos, TileOpticalFiberBase.class).addConnection(this);
//  }
}