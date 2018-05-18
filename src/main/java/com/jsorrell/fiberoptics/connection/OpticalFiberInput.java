package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.transfer_types.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class OpticalFiberInput extends OpticalFiberConnection {
  public OpticalFiberInput(BlockPos pos, EnumFacing connectedSide, TransferType transferType) {
    super(pos, connectedSide, transferType);
  }

  @SuppressWarnings("unused")
  public OpticalFiberInput(ByteBuf buf) {
    super(buf);
  }

  @Override
  public TransferDirection getTransferDirection() {
    return TransferDirection.INPUT;
  }

  @SuppressWarnings("unchecked")
  public boolean isOffering(IBlockAccess worldIn) {
    TileEntity connectedTile = worldIn.getTileEntity(getPos().offset(getConnectedSide()));
    Object capabilityHandler = connectedTile.getCapability(this.getTransferType().getCapability(), this.getConnectedSide().getOpposite());
    return this.getTransferType().isOffering(capabilityHandler);
  }

  public boolean doTransfer(World worldIn, OpticalFiberOutput output) {
    //TODO implement
    return false;
  }
}
