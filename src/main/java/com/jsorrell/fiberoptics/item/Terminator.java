package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.ModBlocks;
import com.jsorrell.fiberoptics.block.optical_fiber.TileOpticalFiberBase;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenSideChooser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Terminator extends ItemBase {

  public static final String REGISTRY_NAME = "terminator";

  public Terminator() {
    super(REGISTRY_NAME);
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.getBlockState(pos).getBlock() == ModBlocks.opticalFiber) {
      if (!worldIn.isRemote) {
        if (playerIn.isSneaking()) {
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenSideChooser(pos), (EntityPlayerMP) playerIn);
        } else {
          System.out.println("send packet open connections");
          TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(pos, facing, tile.getConnections(facing)), (EntityPlayerMP) playerIn);
        }
      }
      return EnumActionResult.SUCCESS;
    }

    return EnumActionResult.FAIL;
  }
}
