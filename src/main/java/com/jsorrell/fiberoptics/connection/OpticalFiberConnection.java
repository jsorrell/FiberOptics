package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import com.jsorrell.fiberoptics.transfer_types.ModTransferTypes;
import com.jsorrell.fiberoptics.transfer_types.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class OpticalFiberConnection {
  private final BlockPos pos;
  private final EnumFacing connectedSide;
  private final TransferType transferType;

  public OpticalFiberConnection(BlockPos pos, EnumFacing connectedSide, TransferType transferType) {
    this.pos = pos;
    this.connectedSide = connectedSide;
    this.transferType = transferType;
  }

  public OpticalFiberConnection(ByteBuf buf) {
    int posX = buf.readInt();
    int posY = buf.readInt();
    int posZ = buf.readInt();
    this.pos = new BlockPos(posX, posY, posZ);
    this.connectedSide = EnumFacing.getFront(buf.readInt());
    this.transferType = ModTransferTypes.fromIndex(buf.readInt());
  }

  public enum TransferDirection {
    INPUT,
    OUTPUT
  }

  public BlockPos getPos() {
    return pos;
  }

  public TileOpticalFiberController getController(IBlockAccess worldIn) {
    TileOpticalFiberBase thisTile = (TileOpticalFiberBase) worldIn.getTileEntity(this.pos);
    return (TileOpticalFiberController)worldIn.getTileEntity(thisTile.getControllerPos());
  }

  public TileEntity getConnectedTile(IBlockAccess worldIn) {
    BlockPos connectedTilePos = this.pos.offset(this.connectedSide);
    return worldIn.getTileEntity(connectedTilePos);
  }

  public EnumFacing getConnectedSide() {
    return connectedSide;
  }

  public TransferType getTransferType() {
    return this.transferType;
  }
  public abstract TransferDirection getTransferDirection();

  public void toBytes(ByteBuf buf) {
    buf.writeInt(pos.getX());
    buf.writeInt(pos.getY());
    buf.writeInt(pos.getZ());
    buf.writeInt(connectedSide.getIndex());
    buf.writeInt(ModTransferTypes.getIndex(transferType));
  }
}