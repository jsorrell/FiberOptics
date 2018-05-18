package com.jsorrell.fiberoptics.transfer_types;

import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.Type;

//TODO improve this api
public abstract class TransferType<T> {
  public abstract Type getTransferObjectType();

  public abstract Capability<?> getCapability();

  public abstract boolean isOffering(T input);

  public abstract boolean doTransfer(T input, T output);
}
