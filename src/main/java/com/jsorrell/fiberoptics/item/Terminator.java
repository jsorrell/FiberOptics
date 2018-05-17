package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.client.gui.TerminatorGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.logging.Level;

public class Terminator extends ItemBase {

  public Terminator() {
    super("terminator");
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote) {
      return EnumActionResult.PASS;
    }

    TileEntity testTile = worldIn.getTileEntity(pos);
    if (!(testTile instanceof TileOpticalFiberBase)) {
      return EnumActionResult.PASS;
    }
    TileOpticalFiberBase tile = (TileOpticalFiberBase) testTile;
    Minecraft.getMinecraft().displayGuiScreen(new TerminatorGui(pos, tile.getPossibleConnections()));

    return EnumActionResult.SUCCESS;
  }
}
