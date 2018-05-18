package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.transfer_type.TransferType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class TileOpticalFiberBase extends TileEntity {
  public abstract BlockPos getControllerPos();
  public abstract boolean isController();

  public boolean canAttachAnySide() {
    return false;
  }

  public class PossibleConnection {
    EnumFacing facing;
    TransferType transferType;
    PossibleConnection(EnumFacing facing, TransferType transferType) {
      this.facing = facing;
      this.transferType = transferType;
    }

    public TransferType getTransferType() {
      return transferType;
    }

    public EnumFacing getFacing() {
      return facing;
    }

    @Override
    public String toString() {
      return facing.toString() + " " + transferType.toString();
    }
  }

  public List<PossibleConnection> getPossibleConnections() {
    List<PossibleConnection> res = new ArrayList<>(6 * ModTransferTypes.VALUES.length);
    for (EnumFacing side : EnumFacing.VALUES) {
      TileEntity testTile = this.world.getTileEntity(this.pos.offset(side));
      if (testTile == null) {
        continue;
      }
      for (TransferType transferType : ModTransferTypes.VALUES) {
        //FIXME side direction specific possible connections - only connects to energy insertions
        if (transferType.isSink(testTile, side.getOpposite()) || transferType.isSource(testTile, side.getOpposite())) {
          PossibleConnection possibleConnection = new PossibleConnection(side, transferType);
          res.add(possibleConnection);
        }
      }
    }

    return res;
  }

  // Only call when sure object should be TileOpticalFiberBase or subclass
  public static TileOpticalFiberBase getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    if (testTile == null) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile Entity does not exist: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }
    if (!(testTile instanceof TileOpticalFiberBase)) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile is not instance of TileOpticalFiberBase: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }

    return (TileOpticalFiberBase) testTile;
  }
}
