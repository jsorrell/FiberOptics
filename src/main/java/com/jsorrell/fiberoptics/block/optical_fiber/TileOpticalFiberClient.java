package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketClientSync;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.BitSet;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileOpticalFiberClient extends TileEntity {
  private final BitSet connectedSides = new BitSet(EnumFacing.VALUES.length);
  private IBlockState cover = null;

  public boolean hasConnectionOnSide(EnumFacing side) {
    return this.connectedSides.get(side.getIndex());
  }

  public void setConnectionOnSide(EnumFacing side, boolean hasConnection) {
    this.connectedSides.set(side.getIndex(), hasConnection);
  }

  @Override
  public void onLoad() {
    FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketClientSync.Request(this.pos));
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound sides = new NBTTagCompound();
    for (EnumFacing side : EnumFacing.VALUES) {
      sides.setBoolean(side.toString(), this.connectedSides.get(side.getIndex()));
    }

    compound.setTag("sides", sides);
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    NBTTagCompound sides = compound.getCompoundTag("sides");
    for (EnumFacing side : EnumFacing.VALUES) {
      this.connectedSides.set(side.getIndex(), sides.getBoolean(side.toString()));
    }
  }
}
