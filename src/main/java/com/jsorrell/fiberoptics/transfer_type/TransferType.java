package com.jsorrell.fiberoptics.transfer_type;

import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

//TODO improve this api
public abstract class TransferType<T> {
  public abstract Capability<?> getCapability();

  public abstract boolean isOffering(@Nonnull T input);

  public abstract boolean doTransfer(@Nonnull T input, @Nonnull T output);

  public boolean isSource(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public boolean isSink(TileEntity tile, EnumFacing side) {
    return tile.hasCapability(getCapability(), side);
  }

  public abstract String getUnlocalizedName();

  public String getName() {
    return I18n.format("transfer_type." + getUnlocalizedName() + ".name");
  }
}