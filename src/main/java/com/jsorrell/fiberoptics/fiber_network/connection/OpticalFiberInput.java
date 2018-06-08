package com.jsorrell.fiberoptics.fiber_network.connection;

import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public final class OpticalFiberInput extends OpticalFiberConnection {
  // Outputs that match transfer type and channel
  public List<OpticalFiberOutput> matchingOutputs;

  public OpticalFiberInput(BlockPos pos, EnumFacing connectedSide, TransferType transferType, String channelName) {
    super(pos, connectedSide, transferType, channelName);
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
    return TransferDirection.EXTRACT;
  }


  /**
   * Checks if the input has things to transfer.
   * @param worldIn the world.
   */
  @SuppressWarnings("unchecked")
  public boolean isOffering(IBlockAccess worldIn) {
    Object inputCapabilityHandler = this.getCapabilityHandler(worldIn);
    if (inputCapabilityHandler == null) {
      return false;
    }
    return this.transferType.isOffering(inputCapabilityHandler);
  }

  /**
   * Attempts to transfer to the output.
   * @param worldIn world.
   * @param output where to transfer to.
   * @return transfer occurred.
   */
  @SuppressWarnings("unchecked")
  public boolean doTransfer(World worldIn, OpticalFiberOutput output) {
    Object inputCapabilityHandler = this.getCapabilityHandler(worldIn);
    Object outputCapabilityHandler = output.getCapabilityHandler(worldIn);
    if (inputCapabilityHandler == null || outputCapabilityHandler == null) {
      return false;
    }
    return this.transferType.doTransfer(inputCapabilityHandler, outputCapabilityHandler);
  }
}
