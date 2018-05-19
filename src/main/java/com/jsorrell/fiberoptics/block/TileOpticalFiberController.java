package com.jsorrell.fiberoptics.block;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.transfer_type.TransferType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.logging.Level;

public class TileOpticalFiberController extends TileOpticalFiberBase implements ITickable {
  private final List<OpticalFiberInput> inputConnections = new ArrayList<>();
  private final List[] outputConnections = new List[ModTransferTypes.VALUES.length];
  private int currentTick = 0;

  private List<OpticalFiberInput> getInputConnections() {
    return this.inputConnections;
  }

  @SuppressWarnings("unchecked")
  private List<OpticalFiberOutput> getOutputConnections(TransferType type) {
    // Lazy instantiation to save memory when most will be null
    int index = ModTransferTypes.getIndex(type);
    List<OpticalFiberOutput> ret = outputConnections[index];
    if (ret == null) {
      ret = new ArrayList<>();
      outputConnections[index] = ret;
    }
    return ret;
  }

  public BlockPos getControllerPos() {
    return this.pos;
  }

  @Override
  public boolean isController() {
    return true;
  }

  void importData(TileOpticalFiberController oldController) {
    //TODO implement
  }

  void splitData(TileOpticalFiberController controller, Set<BlockPos> networkElements) {
    //TODO implement
  }

  public boolean addConnection(OpticalFiberConnection connection) {
    if (connection instanceof OpticalFiberInput) {
      List<OpticalFiberInput> inputs = getInputConnections();
      inputs.add((OpticalFiberInput) connection);
    } else if (connection instanceof OpticalFiberOutput) {
      List<OpticalFiberOutput> outputs = getOutputConnections(connection.getTransferType());
      outputs.add((OpticalFiberOutput) connection);
    }

    this.markDirty();

    System.out.println("Add Connection");
    System.out.println(connection.getClass());
    System.out.println(connection.getPos());
    System.out.println(connection.getConnectedSide());
    System.out.println(connection.getTransferDirection());
    System.out.println(connection.getTransferType());
    return true;
  }

  public void removeAllConnectionsForPos(BlockPos pos) {

  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    /* Write Inputs */
    NBTTagList inputs = new NBTTagList();
    for (int i = 0; i < inputConnections.size(); i++) {
      NBTTagCompound connectionNBT = new NBTTagCompound();
      inputs.appendTag(inputConnections.get(i).writeToNBT(connectionNBT));
    }
    compound.setTag("inputs", inputs);

    /* Write Outputs */
    NBTTagList outputs = new NBTTagList();
    for (int i = 0; i < outputConnections.length; i++) {
      if (outputConnections[i] == null) {
        continue;
      }
      for (int j = 0; j < outputConnections[i].size(); j++) {
        NBTTagCompound connectionNBT = new NBTTagCompound();
        outputs.appendTag(((OpticalFiberOutput)outputConnections[i].get(j)).writeToNBT(connectionNBT));
      }
    }
    compound.setTag("outputs", outputs);

    return super.writeToNBT(compound);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    /* Read Inputs */
    NBTTagList inputs = compound.getTagList("inputs", Constants.NBT.TAG_COMPOUND);
    for (int i = 0; i < inputs.tagCount(); i++) {
      this.addConnection(new OpticalFiberInput(inputs.getCompoundTagAt(i)));
    }

    /* Read Outputs */
    NBTTagList outputs = compound.getTagList("outputs", Constants.NBT.TAG_COMPOUND);
    for (int i = 0; i < outputs.tagCount(); i++) {
      this.addConnection(new OpticalFiberOutput(outputs.getCompoundTagAt(i)));
    }

    super.readFromNBT(compound);
  }

  @Override
  public void update() {
    //TODO parallelize (maybe. possibly slower on small networks)
    for (OpticalFiberInput input : getInputConnections()) {
      if (input.isOffering(world)) {
        List<OpticalFiberOutput> outputs = getOutputConnections(input.getTransferType());
        for (OpticalFiberOutput output : outputs) {
          if (input.doTransfer(world, output)) {
            // only do one transfer per tick for now
            return;
          }
        }
      }
    }
  }

  public static TileOpticalFiberController getTileEntity(IBlockAccess world, BlockPos pos) {
    TileEntity testTile = world.getTileEntity(pos);
    if (testTile == null) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile Entity at " + pos + " does not exist: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }
    if (!(testTile instanceof TileOpticalFiberController)) {
      FiberOptics.LOGGER.log(Level.WARNING, "Tile at " + pos + " is not instance of TileOpticalFiberController: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }

    return (TileOpticalFiberController) testTile;
  }
}
