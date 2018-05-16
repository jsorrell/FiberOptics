package com.jsorrell.fiberoptics.connection;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.lang.reflect.Type;

public abstract class OpticalFiberConnection {
  private BlockPos pos;
  private EnumFacing connectedSide;

  public OpticalFiberConnection(BlockPos pos, EnumFacing connectedSide) {
    this.pos = pos;
    this.connectedSide = connectedSide;
  }

  public OpticalFiberConnection(ByteBuf buf) {
    int posX = buf.readInt();
    int posY = buf.readInt();
    int posZ = buf.readInt();
    this.pos = new BlockPos(posX, posY, posZ);
    this.connectedSide = EnumFacing.getFront(buf.readInt());
  }

  public enum ConnectionType implements IStringSerializable {
    ITEMS("items", ItemStack.class, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY),
    FORGE_FLUIDS("forge_fluids", FluidStack.class, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY),
    FORGE_ENERGY("forge_energy", int.class, CapabilityEnergy.ENERGY);

    String name;
    Type transferObjectType;
    Capability capability;

    ConnectionType(String name, Type transferObjectType, Capability capability) {
      this.name = name;
      this.transferObjectType = transferObjectType;
      this.capability = capability;
    }

    public Type getTransferObjectType() {
      return transferObjectType;
    }

    public Capability getCapability() {
      return capability;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public enum ConnectionDirection {
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

  public abstract ConnectionType getConnectionType();
  public ConnectionDirection getConnectionDirection() {
    if (this instanceof OpticalFiberInput) {
      return ConnectionDirection.INPUT;
    }
    if (this instanceof OpticalFiberOutput) {
      return ConnectionDirection.OUTPUT;
    }
    return null;
  }

  public void toBytes(ByteBuf buf) {
    buf.writeInt(pos.getX());
    buf.writeInt(pos.getY());
    buf.writeInt(pos.getZ());
    buf.writeInt(connectedSide.getIndex());
  }
}