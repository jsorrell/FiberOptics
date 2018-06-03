package com.jsorrell.fiberoptics.block.optical_fiber;

import com.google.common.collect.ImmutableList;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface IConnectionContainer {
  /**
   * Attempts to add a connection to the network.
   * @param connection the connection.
   * @return {@code true} iff the connection was valid and successfully added.
   */
  boolean addConnection(OpticalFiberConnection connection);

  /**
   * Attempts to replace a connection with another in the network.
   * @param connection
   * @param connectionToReplace
   * @return
   */
  boolean replaceConnection(OpticalFiberConnection connection, OpticalFiberConnection connectionToReplace);

  /**
   * Attempts to remove a connection from the network.
   * @param connection the connection.
   * @return {@code true} iff the connection was in the network and successfully removed.
   */
  boolean removeConnection(OpticalFiberConnection connection);

  /**
   * Gets all connections to the {@link TileOpticalFiberBase}.
   * @param side the side to get connections on or null for all sides.
   * @return a list of all connections.
   */
  ImmutableList<OpticalFiberConnection> getConnections(@Nullable EnumFacing side);

  default ImmutableList<OpticalFiberConnection> getConnections() {
    return getConnections(null);
  }

  boolean hasConnectionOnSide(EnumFacing side);
}
