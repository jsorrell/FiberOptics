package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.transfer_type.TransferType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

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

  public OpticalFiberConnection(NBTTagCompound compound) {
    int posX = compound.getInteger("x");
    int posY = compound.getInteger("y");
    int posZ = compound.getInteger("z");
    this.pos = new BlockPos(posX, posY, posZ);
    this.connectedSide = EnumFacing.getFront(compound.getInteger("connectedSide"));
    this.transferType = ModTransferTypes.fromUnlocalizedName(compound.getString("transferType"));
  }

  public enum TransferDirection {
    INPUT,
    OUTPUT
  }

  public BlockPos getPos() {
    return pos;
  }

  public TileEntity getConnectedTile(IBlockAccess worldIn) {
    BlockPos connectedTilePos = this.pos.offset(this.connectedSide);
    return worldIn.getTileEntity(connectedTilePos);
  }

  @SuppressWarnings("unchecked")
  protected Object getCapabilityHandler(IBlockAccess worldIn) {
    TileEntity connectedTile = worldIn.getTileEntity(getPos().offset(getConnectedSide()));
    return connectedTile.getCapability(this.getTransferType().getCapability(), this.getConnectedSide().getOpposite());
  }

  public EnumFacing getConnectedSide() {
    return connectedSide;
  }

  @Nonnull
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

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setInteger("x", this.pos.getX());
    compound.setInteger("y", this.pos.getY());
    compound.setInteger("z", this.pos.getZ());
    compound.setInteger("connectedSide", this.connectedSide.getIndex());
    compound.setString("transferType", this.transferType.getUnlocalizedName());
    return compound;
  }
}