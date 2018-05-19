package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.transfer_type.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.logging.Level;

public class OpticalFiberInput extends OpticalFiberConnection {
  public OpticalFiberInput(BlockPos pos, EnumFacing connectedSide, TransferType transferType) {
    super(pos, connectedSide, transferType);
  }

  @SuppressWarnings("unused")
  public OpticalFiberInput(ByteBuf buf) {
    super(buf);
  }

  public OpticalFiberInput(NBTTagCompound compound) {
    super(compound);
  }

  @Override
  public TransferDirection getTransferDirection() {
    return TransferDirection.INPUT;
  }


  /**
   * Checks if the input has things to transfer.
   * @param worldIn The world
   */
  public boolean isOffering(IBlockAccess worldIn) {
    //TODO remove and ensure that these are never null
    Object inputCapabilityHandler = this.getCapabilityHandler(worldIn);
    if (inputCapabilityHandler == null) {
      FiberOptics.LOGGER.log(Level.WARNING, "Should probably never get here. Handle this case another way.");
      return false;
    }
    return this.getTransferType().isOffering(inputCapabilityHandler);
  }

  /**
   * Attempts to transfer to the output.
   * @param worldIn World
   * @param output Where to transfer to
   * @return Transfer occurred
   */
  public boolean doTransfer(World worldIn, OpticalFiberOutput output) {
    //TODO remove and ensure that these are never null
    Object inputCapabilityHandler = this.getCapabilityHandler(worldIn);
    Object outputCapabilityHandler = output.getCapabilityHandler(worldIn);
    if (inputCapabilityHandler == null || outputCapabilityHandler == null) {
      FiberOptics.LOGGER.log(Level.WARNING, "Should probably never get here. Handle this case another way.");
      return false;
    }
    return this.getTransferType().doTransfer(inputCapabilityHandler, outputCapabilityHandler);
  }
}
