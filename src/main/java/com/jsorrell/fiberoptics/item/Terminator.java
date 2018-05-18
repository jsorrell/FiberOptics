package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.FiberOpticsGuiHandler;
import com.jsorrell.fiberoptics.block.ModBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Terminator extends ItemBase {

  public Terminator() {
    super("terminator");
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.getBlockState(pos).getBlock() != ModBlocks.opticalFiber) {
      return EnumActionResult.PASS;
    }

    player.openGui(FiberOptics.instance, FiberOpticsGuiHandler.TERMINATOR, worldIn, pos.getX(), pos.getY(), pos.getZ());
    return EnumActionResult.SUCCESS;
  }
}
