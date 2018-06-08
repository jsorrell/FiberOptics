package com.jsorrell.fiberoptics.fiber_network.connection;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
public class OpticalFiberConnectionFactory {
  public final BlockPos pos;
  public final EnumFacing side;

  private OpticalFiberConnection.TransferDirection direction = null;
  private String channel = null;
  private Integer priority = null;
  private TransferType transferType = null;

  public OpticalFiberConnectionFactory(BlockPos pos, EnumFacing side) {
    this.pos = pos;
    this.side = side;
  }

  public OpticalFiberConnectionFactory(OpticalFiberConnection connectionToCopy) {
    this(connectionToCopy.pos, connectionToCopy.connectedSide);
    this.direction = connectionToCopy.getTransferDirection();
    this.channel = connectionToCopy.channelName;
    this.transferType = connectionToCopy.transferType;
    if (connectionToCopy instanceof OpticalFiberOutput) {
      this.priority = ((OpticalFiberOutput) connectionToCopy).priority;
    }
  }

  public OpticalFiberConnectionFactory setDirection(OpticalFiberConnection.TransferDirection direction) {
    this.direction = direction;
    return this;
  }

  public OpticalFiberConnectionFactory setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public OpticalFiberConnectionFactory setPriority(int priority) {
    this.priority = priority;
    return this;
  }

  public OpticalFiberConnectionFactory setTransferType(TransferType transferType) {
    this.transferType = transferType;
    return this;
  }


  public OpticalFiberConnection getConnection() throws NonDefiningConnectionException {
    Stack<ConnectionInfoType> missingInfo = new Stack<>();

    if (this.direction == null) missingInfo.push(ConnectionInfoType.DIRECTION);
    if (this.channel == null) missingInfo.push(ConnectionInfoType.CHANNEL);
    if (this.transferType == null) missingInfo.push(ConnectionInfoType.TRANSFER_TYPE);
    if (this.direction.equals(OpticalFiberConnection.TransferDirection.INSERT) && this.priority == null) {
      missingInfo.push(ConnectionInfoType.PRIORITY);
    }

    if (!missingInfo.empty()) throw new NonDefiningConnectionException(missingInfo);

    if (this.direction.equals(OpticalFiberConnection.TransferDirection.EXTRACT)) return new OpticalFiberInput(this.pos, this.side, this.transferType, this.channel);
    else return new OpticalFiberOutput(this.pos, this.side, this.transferType, this.channel, this.priority);
  }

  public enum ConnectionInfoType {
    POSITION("position"),
    SIDE("side"),
    DIRECTION("direction"),
    CHANNEL("channel"),
    PRIORITY("priority"),
    TRANSFER_TYPE("config");

    private final String name;

    ConnectionInfoType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static class NonDefiningConnectionException extends Exception {
    public final ImmutableList<ConnectionInfoType> missingInfo;

    NonDefiningConnectionException(Collection<ConnectionInfoType> missingInfo) {
      if (missingInfo.isEmpty()) {
        throw new IllegalArgumentException("missingInfo must not be empty.");
      }

      this.missingInfo = ImmutableList.copyOf(missingInfo);
    }

    @Override
    public String toString() {
      Iterator<ConnectionInfoType> iterator = this.missingInfo.iterator();
      StringBuilder str = new StringBuilder();
      str.append(iterator.next().toString());
      while (iterator.hasNext()) {
        str.append(", ");
        str.append(iterator.next());
      }
      return "NonDefiningConnection: Missing " + str;
    }
  }
}
