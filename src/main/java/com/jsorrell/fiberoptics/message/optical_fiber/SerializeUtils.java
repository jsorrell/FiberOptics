package com.jsorrell.fiberoptics.message.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SerializeUtils {
  /* ByteBuf */
  public static void writeBlockPos(ByteBuf buf, BlockPos pos) {
    buf.writeLong(pos.toLong());
  }

  public static BlockPos readBlockPos(ByteBuf buf) {
    return BlockPos.fromLong(buf.readLong());
  }

  /* EnumFacing */
  public static void writeEnumFacing(ByteBuf buf, EnumFacing side) {
    buf.writeByte(side.getIndex());
  }

  public static EnumFacing readEnumFacing(ByteBuf buf) {
    return EnumFacing.getFront(buf.readByte());
  }

  /* String */
  public static void writeUTF8String(ByteBuf buf, String string) {
    ByteBufUtils.writeUTF8String(buf, string);
  }

  public static String readUTF8String(ByteBuf buf) {
    return ByteBufUtils.readUTF8String(buf);
  }

  /* TransferType */
  public static void writeTransferType(ByteBuf buf, TransferType type) {
    writeUTF8String(buf, type.getRegistryKey().toString());
  }

  @Nullable
  public static TransferType readTransferType(ByteBuf buf) {
    ResourceLocation key = new ResourceLocation(readUTF8String(buf));
    if (!TransferType.hasTypeForKey(key)) return null;
    return TransferType.getTypeFromKey(key);
  }

  /* List */
  public static <T> void writeList(ByteBuf buf, List<T> list, BiConsumer<ByteBuf, T> writer) {
    buf.writeInt(list.size());
    for (T e : list) {
      writer.accept(buf, e);
    }
  }

  public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> reader) {
    List<T> out = new ArrayList<>();
    int length = buf.readInt();
    for (int i = 0; i < length; ++i) {
      out.add(reader.apply(buf));
    }
    return out;
  }

  /* Collection */
  public static <T> void writeCollection(ByteBuf buf, Collection<T> collection, BiConsumer<ByteBuf, T> writer) {
    buf.writeInt(collection.size());
    for (T e : collection) {
      writer.accept(buf, e);
    }
  }

  public static <T> Collection<T> readCollection(ByteBuf buf, Function<ByteBuf, T> reader) {
    return readList(buf, reader);
  }
}

