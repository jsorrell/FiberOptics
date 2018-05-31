package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketClientSync;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.BitSet;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileOpticalFiberClient extends TileEntity {
  private final BitSet connectedSides = new BitSet(EnumFacing.VALUES.length);
  private IBlockState cover = null;

  public boolean hasConnectionOnSide(EnumFacing side) {
    return this.connectedSides.get(side.ordinal());
  }

  public void setConnectionOnSide(EnumFacing side, boolean hasConnection) {
    this.connectedSides.set(side.getIndex(), hasConnection);
  }

  @Override
  public void onLoad() {
    FiberOpticsPacketHandler.INSTANCE.sendToServer(new PacketClientSync.Request(this.pos));
  }

  /**
   * Gets the tile entity of type {@link TileOpticalFiberClient} from the world.
   * Only call this when sure that the tile exists and is of this type.
   * @param world the world.
   * @param pos the position of the tile.
   * @return the tile entity of type {@link TileOpticalFiberClient}.
   */
  public static TileOpticalFiberClient getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    Objects.requireNonNull(testTile, "Tile Entity at " + pos + " does not exist");
    if (!(testTile instanceof TileOpticalFiberClient)) {
      throw new ClassCastException("Tile at " + pos + "  is not instance of TileOpticalFiberClient");
    }
    return (TileOpticalFiberClient) testTile;
  }
}
