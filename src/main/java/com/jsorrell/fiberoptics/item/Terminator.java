package com.jsorrell.fiberoptics.item;

import com.jsorrell.fiberoptics.block.ModBlocks;
import com.jsorrell.fiberoptics.block.optical_fiber.*;
import com.jsorrell.fiberoptics.message.FiberOpticsPacketHandler;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenConnectionChooser;
import com.jsorrell.fiberoptics.message.optical_fiber.PacketOpenSideChooser;
import com.jsorrell.fiberoptics.utils.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

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
          if (BlockOpticalFiber.getBoundingBoxForCenter().contains(hitVec)) {
            // If we hit the center part, the direction is the side
            side = facing;
          } else {
            IBlockState state = worldIn.getBlockState(pos);
            // If we hit a side, we use this side
            for (EnumFacing testSide : EnumFacing.VALUES) {
              Optional<AxisAlignedBB> boxOpt = BlockOpticalFiber.getBoundingBoxForPart(state, FiberPart.fromSide(testSide));
              if (boxOpt.isPresent() && boxOpt.get().contains(hitVec)) {
                side = testSide;
                if (state.getValue(BlockOpticalFiber.getPropertyFromSide(side)) == FiberSideType.SELF_ATTACHMENT) {
                  OpticalFiberUtil.splitConnection(worldIn, pos, side);
                  return EnumActionResult.SUCCESS;
                }
                break;
              }
            }
          }
          assert side != null;

          TileOpticalFiberBase tile = Util.getTileChecked(worldIn, pos, TileOpticalFiberBase.class);
          FiberOpticsPacketHandler.INSTANCE.sendTo(new PacketOpenConnectionChooser(pos, side, tile.getConnections(side)), (EntityPlayerMP) playerIn);
        }
      }
      return EnumActionResult.SUCCESS;
    }

    return EnumActionResult.FAIL;
  }
}
