package com.jsorrell.fiberoptics.block.optical_fiber;

import com.jsorrell.fiberoptics.fiber_network.FiberNetwork;
import com.jsorrell.fiberoptics.utils.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TileOpticalFiberController extends TileOpticalFiberBase implements ITickable {
  final FiberNetwork fiberNetwork = new FiberNetwork();
  /**
   * Set of positions of all blocks in the network. Includes {@code this}.
   */
  final HashSet<BlockPos> networkBlocks = new HashSet<>();

  // Needed by Forge
  @SuppressWarnings({"unused", "WeakerAccess"})
  public TileOpticalFiberController() {
    super();
  }

  @Override
  void importConnections(TileOpticalFiberBase fiber) {
    this.fiberNetwork.addAllConnections(fiber.getConnections());
    super.importConnections(fiber);
  }

  @Override
  public void setPos(BlockPos posIn) {
    assert this.pos == BlockPos.ORIGIN || this.pos.equals(posIn); //FIXME This may not be true for things like mekanism boxes
    this.pos = posIn.toImmutable();
    this.networkBlocks.add(posIn.toImmutable());
  }

  @Override
  public BlockPos getControllerPos() {
    return this.pos;
  }

  @Override
  public TileOpticalFiberController getController() {
    return this;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setTag("network", this.fiberNetwork.serializeNBT());
    NBTTagList connectedBlocks = new NBTTagList();
    for (BlockPos pos : this.networkBlocks) {
      NBTTagCompound posComp = new NBTTagCompound();
      posComp.setInteger("x", pos.getX());
      posComp.setInteger("y", pos.getY());
      posComp.setInteger("z", pos.getZ());
      connectedBlocks.appendTag(posComp);
    }
    compound.setTag("blocks", connectedBlocks);
    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    this.fiberNetwork.deserializeNBT(compound.getCompoundTag("network"));
    NBTTagList connectedBlocks = compound.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
    for (int i = 0; i < connectedBlocks.tagCount(); i++) {
      NBTTagCompound posComp = connectedBlocks.getCompoundTagAt(i);
      this.networkBlocks.add(new BlockPos(posComp.getInteger("x"), posComp.getInteger("y"), posComp.getInteger("z")));
    }
  }

  @Override
  public void update() {
    this.fiberNetwork.doTransfers(this.world);
  }

  @Override
  public void onChunkUnload() {
    //TODO
    super.onChunkUnload();
  }
}
