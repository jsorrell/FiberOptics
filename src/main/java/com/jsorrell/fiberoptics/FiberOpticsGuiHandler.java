package com.jsorrell.fiberoptics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class FiberOpticsGuiHandler implements IGuiHandler {
//  public static final int TERMINATOR_SIDE_CHOOSER = 0;
//  public static final int TERMINATOR_CONNECTION_CHOOSER = 1;

  @Nullable
  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    switch (ID) {
    }
    return null;
  }

  @Nullable
  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    switch (ID) {
//      case TERMINATOR_SIDE_CHOOSER: {
//        return new SideChooserGui(TileOpticalFiberBase.getTileEntity(world, new BlockPos(x, y, z)));
//      }
//      case TERMINATOR_CONNECTION_CHOOSER: {
//        return new ConnectionChooserGui(TileOpticalFiberBase.getTileEntity(world, new BlockPos(x, y, z)), EnumFacing.UP);
//      }
    }
    return null;
  }
}
