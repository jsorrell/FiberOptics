package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.transfer_types.TransferType;
import io.netty.buffer.ByteBuf;
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


  /**
   * Checks if the input has things to transfer.
   * @param worldIn
   */
  public boolean isOffering(IBlockAccess worldIn) {
    return this.getTransferType().isOffering(getCapabilityHandler(worldIn));
  }

  /**
   * Attempts to transfer to the output.
   * @param worldIn World
   * @param output Where to transfer to
   * @return Transfer occurred
   */
  public boolean doTransfer(World worldIn, OpticalFiberOutput output) {
    return this.getTransferType().doTransfer(getCapabilityHandler(worldIn), output.getCapabilityHandler(worldIn));
  }
}
