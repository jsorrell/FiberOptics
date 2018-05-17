package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class TileOpticalFiberBase extends TileEntity {
  public abstract BlockPos getControllerPos();
  public abstract boolean isController();

  public boolean canAttachAnySide() {
    return false;
  }

  public class PossibleConnection {
    EnumFacing direction;
    OpticalFiberConnection.ConnectionType connectionType;
    PossibleConnection(EnumFacing direction, OpticalFiberConnection.ConnectionType connectionType) {
      this.direction = direction;
      this.connectionType = connectionType;
    }

    public OpticalFiberConnection.ConnectionType getConnectionType() {
      return connectionType;
    }

    public EnumFacing getDirection() {
      return direction;
    }

    @Override
    public String toString() {
      return direction.toString() + " " + connectionType.toString();
    }
  }

  public List<PossibleConnection> getPossibleConnections() {
    List<PossibleConnection> res = new ArrayList<>(6 * OpticalFiberConnection.ConnectionType.values().length);
    for (EnumFacing direction : EnumFacing.VALUES) {
      TileEntity testTile = this.world.getTileEntity(this.pos.offset(direction));
      if (testTile == null) {
        continue;
      }
      for (OpticalFiberConnection.ConnectionType connectionType : OpticalFiberConnection.ConnectionType.values())
      if (testTile.hasCapability(connectionType.getCapability(), direction.getOpposite())) {
        PossibleConnection possibleConnection = new PossibleConnection(direction, connectionType);
        res.add(possibleConnection);
      }
    }

    return res;
  }
}
