package com.jsorrell.fiberoptics;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.client.gui.TerminatorGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class FiberOpticsGuiHandler implements IGuiHandler {
  public static final int TERMINATOR = 0;

  @Nullable
  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    switch (ID) {
      case TERMINATOR: {
        return null;
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    switch (ID) {
      case TERMINATOR: {
        return new TerminatorGui(TileOpticalFiberBase.getTileEntity(world, new BlockPos(x, y, z)));
      }
    }
    return null;
  }
}
