package com.jsorrell.fiberoptics.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class MessagePos implements IMessage {
  protected BlockPos pos = null;

  public MessagePos() {}

  public MessagePos(BlockPos pos) {
    this.pos = pos.toImmutable();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong()).toImmutable();
  }
}
