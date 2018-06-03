package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.optical_fiber.*;
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
    if (!(worldIn.getBlockState(pos).getBlock() instanceof BlockOpticalFiber)) return EnumActionResult.PASS;


    if (worldIn.isRemote) {
      player.sendStatusMessage(new TextComponentString("\n\n\n"), false);
      player.sendStatusMessage(new TextComponentString("§2Client:§r"), false);
      TileEntity testTile = worldIn.getTileEntity(pos);
      if (testTile == null) {
        player.sendStatusMessage(new TextComponentString("§4No tile entity present for optical fiber at " + pos + "§r"), false);
      } else if (!(testTile instanceof TileOpticalFiberClient)) {
        player.sendStatusMessage(new TextComponentString("§4Tile is of incorrect type " + testTile.getClass() + " at " + pos + "§r"), false);
      } else {
        TileOpticalFiberClient tile = (TileOpticalFiberClient) testTile;
        player.sendStatusMessage(new TextComponentString(tile.serializeNBT().toString()), false);
      }
    } else {
      player.sendStatusMessage(new TextComponentString("§9Server:§r"), false);
      TileEntity testTile = worldIn.getTileEntity(pos);
      if (testTile == null) {
        player.sendStatusMessage(new TextComponentString("§4No tile entity present for optical fiber at " + pos + "§r"), false);
      } else if (!(testTile instanceof TileOpticalFiberBase)) {
        player.sendStatusMessage(new TextComponentString("§4Tile is of incorrect type " + testTile.getClass() + " at " + pos + "§r"), false);
      } else {
        if (testTile instanceof TileOpticalFiberController) {
          TileOpticalFiberController tile = (TileOpticalFiberController) testTile;
          player.sendStatusMessage(new TextComponentString("§eController:§r " + tile.serializeNBT().toString()), false);
        } else {
          TileOpticalFiber tile = (TileOpticalFiber) testTile;
          player.sendStatusMessage(new TextComponentString(tile.serializeNBT().toString()), false);
        }
      }
    }

    return EnumActionResult.SUCCESS;
  }
}
