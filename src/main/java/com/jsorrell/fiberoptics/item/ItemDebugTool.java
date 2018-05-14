package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.BlockOpticalFiberBase;
import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
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
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    Block targetBlock = world.getBlockState(pos).getBlock();
    if (targetBlock instanceof BlockOpticalFiberBase) {
      TileOpticalFiberController controller = ((TileOpticalFiberBase)world.getTileEntity(pos)).getControllerTile();
      if (controller == null) {
        player.sendStatusMessage(new TextComponentString("Controller Position: No controller saved."), false);
      } else {
        player.sendStatusMessage(new TextComponentString("Controller Position: " + controller.getPos().toString()), false);
      }
    }
    return EnumActionResult.PASS;
  }
}
