package com.jsorrell.fiberoptics.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Util {

  /**
   * Gets the tile entity of type {@code clazz} from the world.
   * Only call this when sure that the tile exists and is of this type.
   * @param world the world.
   * @param pos the position of the tile.
   * @param clazz the type of tile to get.
   * @return the tile entity as type {@link TileEntity}.
   */
  public static <T extends TileEntity> T getTileChecked(IBlockAccess world, BlockPos pos, Class<T> clazz) {
    TileEntity testTile = world.getTileEntity(pos);
    Objects.requireNonNull(testTile, "Tile Entity at " + pos + " does not exist");
    if (!(clazz.isAssignableFrom(testTile.getClass()))) {
      throw new ClassCastException("Tile at " + pos + "  is not instance of " + clazz.getSimpleName());
    }
    return clazz.cast(testTile);
  }

  /**
   * Returns the value of the first argument raised to the power of the second argument.
   * @param a the base.
   * @param b the power.
   * @return the value of the first argument raised to the power of the second argument.
   */
  public static long iPow(long a, int b)
  {
    if (b == 0) return 1;
    if (b == 1) return a;
    if (b % 2 == 0) return iPow(a*a, b/2);
    return a * iPow (a*a, b/2);
  }
}
