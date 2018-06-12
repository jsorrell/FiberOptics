package com.jsorrell.fiberoptics.fiber_network.connection;

import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
public abstract class OpticalFiberConnectionFactory {
  public final BlockPos pos;
  public final EnumFacing side;
  public String channel = null;

  public OpticalFiberConnectionFactory(BlockPos pos, EnumFacing side) {
    this.pos = pos;
    this.side = side;
  }

  public OpticalFiberConnectionFactory(OpticalFiberConnection connectionToCopy) {
    this(connectionToCopy.pos, connectionToCopy.side);
    this.channel = connectionToCopy.channelName;
  }

  public OpticalFiberConnectionFactory setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public abstract OpticalFiberConnection getConnection() throws NonDefiningConnectionException;

  public static class NonDefiningConnectionException extends Exception {
    public final ImmutableList<String> missingInfo;

    NonDefiningConnectionException(Collection<String> missingInfo) {
      if (missingInfo.isEmpty()) {
        throw new IllegalArgumentException("missingInfo must not be empty.");
      }

      this.missingInfo = ImmutableList.copyOf(missingInfo);
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder();
      this.missingInfo.forEach(s -> str.append(", ").append(s));
      return "NonDefiningConnection: Missing " + str;
    }
  }
}
