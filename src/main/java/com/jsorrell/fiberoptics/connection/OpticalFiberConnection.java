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
  private final BlockPos pos;
  private final EnumFacing connectedSide;
  private final ConnectionType connectionType;

  public OpticalFiberConnection(BlockPos pos, EnumFacing connectedSide, ConnectionType connectionType) {
    this.pos = pos;
    this.connectedSide = connectedSide;
    this.connectionType = connectionType;
  }

  public OpticalFiberConnection(ByteBuf buf) {
    int posX = buf.readInt();
    int posY = buf.readInt();
    int posZ = buf.readInt();
    this.pos = new BlockPos(posX, posY, posZ);
    this.connectedSide = EnumFacing.getFront(buf.readInt());
    this.connectionType = ConnectionType.fromIndex(buf.readInt());
  }

  public enum ConnectionType implements IStringSerializable {
    ITEMS(0, "items", ItemStack.class, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY),
    FORGE_FLUIDS(1, "forge_fluids", FluidStack.class, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY),
    FORGE_ENERGY(2, "forge_energy", int.class, CapabilityEnergy.ENERGY);

    private final int index;
    private String name;
    private Type transferObjectType;
    private Capability capability;

    ConnectionType(int index, String name, Type transferObjectType, Capability capability) {
      this.index = index;
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

    @Override
    public String toString() {
      switch (this) {
        case ITEMS:
          return "items";
        case FORGE_ENERGY:
          return "energy";
        case FORGE_FLUIDS:
          return "fluids";
      }
      return "";
    }

    public static ConnectionType fromIndex(int index) {
      return ConnectionType.values()[index];
    }

    public int getIndex() {
      return this.index;
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

  public ConnectionType getConnectionType() {
    return this.connectionType;
  }
  public abstract ConnectionDirection getConnectionDirection();

  public void toBytes(ByteBuf buf) {
    buf.writeInt(pos.getX());
    buf.writeInt(pos.getY());
    buf.writeInt(pos.getZ());
    buf.writeInt(connectedSide.getIndex());
    buf.writeInt(connectionType.getIndex());
  }
}