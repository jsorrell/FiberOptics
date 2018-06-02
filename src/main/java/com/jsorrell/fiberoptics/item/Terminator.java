package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.ModBlocks;
import com.jsorrell.fiberoptics.block.optical_fiber.BlockOpticalFiber;
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
import net.minecraft.util.math.Vec3d;
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
          Vec3d hitVec = new Vec3d(hitX, hitY, hitZ);
          EnumFacing side = null;
          if (BlockOpticalFiber.getBoundingBoxForConnection(null).contains(hitVec)) {
            side = facing;
          } else {
            for (EnumFacing testSide : EnumFacing.VALUES) {
              if (BlockOpticalFiber.getBoundingBoxForConnection(testSide).contains(hitVec)) {
                side = testSide;
                break;
              }
            }
          }
          assert side != null;
          TileOpticalFiberBase tile = TileOpticalFiberBase.getTileEntity(worldIn, pos);
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(pos, side, tile.getConnections(side)), (EntityPlayerMP) playerIn);
        }
      }
      return EnumActionResult.SUCCESS;
    }

    return EnumActionResult.FAIL;
  }
}
