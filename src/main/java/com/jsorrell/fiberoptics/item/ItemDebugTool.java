package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.BlockOpticalFiber;
import com.jsorrell.fiberoptics.block.ModBlocks;
import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.block.TileOpticalFiberController;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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
      return EnumActionResult.PASS;
    }

    Block targetBlock = worldIn.getBlockState(pos).getBlock();
    if (targetBlock instanceof BlockOpticalFiber) {
      TileOpticalFiberBase tile = ((TileOpticalFiberBase) worldIn.getTileEntity(pos));
      if (tile == null) {
        player.sendStatusMessage(new TextComponentString("No tile entity present for optical fiber at " + pos), false);
        return EnumActionResult.PASS;
      }

      TileOpticalFiberController controller;

      if (tile.isController()) {
        player.sendStatusMessage(new TextComponentString("Is Controller"), false);
        controller = (TileOpticalFiberController)tile;
      } else {
        BlockPos controllerPos = tile.getControllerPos();
        if (controllerPos == null) {
          player.sendStatusMessage(new TextComponentString("Controller Position: No controller saved."), false);
          return EnumActionResult.PASS;
        }
        TileEntity storedController = worldIn.getTileEntity(controllerPos);
        if (!(storedController instanceof TileOpticalFiberController)) {
          player.sendStatusMessage(new TextComponentString("Controller stored is invalid: " + storedController), false);
          return EnumActionResult.PASS;
        }
        controller = ((TileOpticalFiberController) storedController);
      }

      if (player.isSneaking()) {
        player.sendStatusMessage(new TextComponentString("Network: " + ModBlocks.opticalFiber.getNetworkedFibers(worldIn, pos)), false);
      } else {
        player.sendStatusMessage(new TextComponentString("Controller Position: " + controller.getPos()), false);
      }
    }
    return EnumActionResult.PASS;
  }
}
