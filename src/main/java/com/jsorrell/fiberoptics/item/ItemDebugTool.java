package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.OpticalFiber.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.OpticalFiber.TileOpticalFiberBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDebugTool extends ItemBase {
  public ItemDebugTool() {
    super("debug_tool");
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote) {
      player.sendStatusMessage(new TextComponentString("Client:"), false);
    } else {
      player.sendStatusMessage(new TextComponentString("Server:"), false);
    }

    Block targetBlock = worldIn.getBlockState(pos).getBlock();
    if (targetBlock instanceof BlockOpticalFiber) {
      TileOpticalFiberBase tile = ((TileOpticalFiberBase) worldIn.getTileEntity(pos));
      if (tile == null) {
        player.sendStatusMessage(new TextComponentString("No tile entity present for optical fiber at " + pos), false);
        return EnumActionResult.PASS;
      }

      player.sendStatusMessage(new TextComponentString(System.identityHashCode(tile) + " : " + tile.serializeNBT().toString()), false);
    }
    return EnumActionResult.PASS;
  }
}
