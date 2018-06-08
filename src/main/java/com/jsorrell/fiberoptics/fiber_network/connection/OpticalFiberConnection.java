package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import com.jsorrell.fiberoptics.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;

/**
 * Immutable
 */
public abstract class OpticalFiberConnection {
  public final BlockPos pos;
  public final EnumFacing connectedSide;
  public final TransferType transferType;
  public final String channelName;
  public static Comparator<OpticalFiberConnection> COMPARATOR = (t0, t1) -> {
    int cmp;
    // Provide consistency with equals
    cmp = t0.pos.compareTo(t1.pos);
    if (cmp != 0) return cmp;
    cmp = t0.connectedSide.compareTo(t1.connectedSide);
    if (cmp != 0) return cmp;
    cmp = Integer.compare(ModTransferTypes.getIndex(t0.transferType), ModTransferTypes.getIndex(t1.transferType));
    if (cmp != 0) return cmp;
    cmp = t0.channelName.compareTo(t1.channelName);
    return cmp;
  };

  public OpticalFiberConnection(@Nonnull BlockPos pos, @Nonnull EnumFacing connectedSide, @Nonnull TransferType transferType, @Nonnull String channelName) {
    this.pos = pos.toImmutable();
    this.connectedSide = connectedSide;
    this.transferType = transferType;
    this.channelName = channelName;
  }

  public OpticalFiberConnection(ByteBuf buf) {
    int posX = buf.readInt();
    int posY = buf.readInt();
    int posZ = buf.readInt();
    this.pos = new BlockPos(posX, posY, posZ).toImmutable();
    this.connectedSide = EnumFacing.getFront(buf.readInt());
    this.transferType = ModTransferTypes.fromIndex(buf.readInt());
    this.channelName = ByteBufUtils.readUTF8String(buf);
  }

  public OpticalFiberConnection(NBTTagCompound compound) {
    int posX = compound.getInteger("x");
    int posY = compound.getInteger("y");
    int posZ = compound.getInteger("z");
    this.pos = new BlockPos(posX, posY, posZ).toImmutable();
    this.connectedSide = EnumFacing.getFront(compound.getInteger("connectedSide"));
    this.transferType = ModTransferTypes.fromUnlocalizedName(compound.getString("config"));
    this.channelName = compound.getString("channel");
  }

  public enum TransferDirection {
    EXTRACT("extract"), // Extract from tile entity into network
    INSERT("insert"); // Insert into tile entity from network
    private final String unlocalizedName;

    TransferDirection(String unlocalizedName) {
      this.unlocalizedName = unlocalizedName;
    }

    public String getUnlocalizedName() {
      return this.unlocalizedName;
    }

    public String getName() {
      return I18n.format("transfer_direction." + getUnlocalizedName() + ".name");
    }
  }

  public static OpticalFiberConnection fromKeyedBytes(ByteBuf buf) {
    int transferDirection = buf.readInt();
    if (transferDirection == TransferDirection.EXTRACT.ordinal()) {
      return new OpticalFiberInput(buf);
    } else {
      return new OpticalFiberOutput(buf);
    }
  }

  public TileEntity getConnectedTile(IBlockAccess worldIn) {
    BlockPos connectedTilePos = this.pos.offset(this.connectedSide);
    return worldIn.getTileEntity(connectedTilePos);
  }

  @Nullable
  protected Object getCapabilityHandler(IBlockAccess worldIn) {
    TileEntity connectedTile = worldIn.getTileEntity(pos.offset(this.connectedSide));
    if (connectedTile == null) {
      return null;
    }
    Capability<?> capability = this.transferType.getCapability();
    return connectedTile.getCapability(capability, this.connectedSide.getOpposite());
  }

  public abstract TransferDirection getTransferDirection();

  public void toBytes(ByteBuf buf) {
    buf.writeInt(pos.getX());
    buf.writeInt(pos.getY());
    buf.writeInt(pos.getZ());
    buf.writeInt(connectedSide.getIndex());
    buf.writeInt(ModTransferTypes.getIndex(transferType));
    ByteBufUtils.writeUTF8String(buf, channelName);
  }

  public void toKeyedBytes(ByteBuf buf) {
    buf.writeInt(this.getTransferDirection().ordinal());
    this.toBytes(buf);
  }

  public NBTTagCompound serializeNBT() {
    NBTTagCompound compound = new NBTTagCompound();
    compound.setInteger("x", this.pos.getX());
    compound.setInteger("y", this.pos.getY());
    compound.setInteger("z", this.pos.getZ());
    compound.setInteger("connectedSide", this.connectedSide.getIndex());
    compound.setString("config", this.transferType.getUnlocalizedName());
    compound.setString("channel", this.channelName);
    return compound;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OpticalFiberConnection)) return false;
    OpticalFiberConnection that = (OpticalFiberConnection) o;
    return Objects.equals(pos, that.pos) &&
            connectedSide == that.connectedSide &&
            Objects.equals(transferType, that.transferType) &&
            Objects.equals(channelName, that.channelName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pos, connectedSide, transferType, channelName);
  }

  /**
   * Add the connection to the world.
   * @param world the world.
   * @return {@code true} iff the connection was successfully added.
   */
  public boolean initialize(IBlockAccess world) {
    return Util.getTileChecked(world, this.pos, TileOpticalFiberBase.class).addConnection(this);
  }
}