package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.Objects;

public final class OpticalFiberOutput extends OpticalFiberConnection {
  public final int priority;
  public static Comparator<OpticalFiberOutput> PRIORITY_COMPARATOR = (t0, t1) ->{
    int cmp;
    // Priority is used as the main comparison so that outputs are ordered by priority later
    cmp = Integer.compare(t0.priority, t1.priority);
    if (cmp != 0) return cmp;
    return OpticalFiberConnection.COMPARATOR.compare(t0, t1);
  };

  public OpticalFiberOutput(BlockPos pos, EnumFacing connectedSide, TransferType transferType, String channelName) {
    this(pos, connectedSide, transferType, channelName, 0);
  }

  public OpticalFiberOutput(BlockPos pos, EnumFacing connectedSide, TransferType transferType, String channelName, int priority) {
    super(pos, connectedSide, transferType, channelName);
    this.priority = priority;
  }

  @SuppressWarnings("unused")
  public OpticalFiberOutput(ByteBuf buf) {
    super(buf);
    this.priority = buf.readInt();
  }

  public OpticalFiberOutput(NBTTagCompound compound) {
    super(compound);
    this.priority = compound.getInteger("priority");
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeInt(this.priority);
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound compound = super.serializeNBT();
    compound.setInteger("priority", this.priority);
    return compound;
  }

  @Override
  public TransferDirection getTransferDirection() {
    return TransferDirection.INSERT;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OpticalFiberOutput)) return false;
    if (!super.equals(o)) return false;
    OpticalFiberOutput that = (OpticalFiberOutput) o;
    return priority == that.priority;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), priority);
  }
}