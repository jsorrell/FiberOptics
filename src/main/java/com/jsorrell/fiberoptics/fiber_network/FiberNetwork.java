package com.jsorrell.fiberoptics.fiber_network;

import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberInput;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberOutput;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.ModTransferTypes;
import com.jsorrell.fiberoptics.fiber_network.transfer_type.TransferType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FiberNetwork implements INBTSerializable<NBTTagCompound> {
  private final List<List<Channel>> existingChannels = new ArrayList<>(ModTransferTypes.VALUES.length);

  /**
   * Output list ordered by priority
   */
  private final Map<Tuple<TransferType, String>, Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel>> connections = new HashMap<>();

  public FiberNetwork() {
    // Initialize lists in existingChannels
    for (int i = 0; i < ModTransferTypes.VALUES.length; ++i) {
      existingChannels.add(i, new ArrayList<>());
    }
  }

  /**
   * Adds a connection to the network.
   * @param connection the connection to add.
   */
  public void addConnection(OpticalFiberConnection connection) {
    boolean needCreate;
    // Update connections
    Tuple<TransferType, String> subnetwork = new Tuple<>(connection.transferType, connection.channelName);
    Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connections = this.connections.get(subnetwork);
    if (needCreate = connections == null) {
      connections = new Threetuple<>(new HashSet<>(), new TreeSet<>(OpticalFiberOutput.PRIORITY_COMPARATOR), new Channel(connection.channelName));
      // Add channel to existingChannels
      this.existingChannels.get(ModTransferTypes.getIndex(connection.transferType)).add(connections.z);
    }

    if (connection.getTransferDirection() == OpticalFiberConnection.TransferDirection.EXTRACT) {
      Set<OpticalFiberInput> inputConnections = connections.x;
      inputConnections.add((OpticalFiberInput) connection);
    } else {
      Set<OpticalFiberOutput> outputConnections = connections.y;
      outputConnections.add((OpticalFiberOutput) connection);
    }

    connections.z.markUse();

    if (needCreate) this.connections.put(subnetwork, connections);
  }

  /**
   * Same as {@link FiberNetwork#addConnection(OpticalFiberConnection)} but assumes the channel has already been added to {@link FiberNetwork#existingChannels}.
   * Doesn't modify {@link FiberNetwork#existingChannels} or any {@link Channel}.
   * @param connection the connection to add.
   */
  private void restoreConnection(OpticalFiberConnection connection) {
    boolean needCreate;
    Channel channel = this.existingChannels.get(ModTransferTypes.getIndex(connection.transferType)).stream().filter(c -> c.channelName.equals(connection.channelName)).findFirst().orElse(null);
    assert channel != null;
    // Update connections
    Tuple<TransferType, String> subnetwork = new Tuple<>(connection.transferType, connection.channelName);
    Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connections = this.connections.get(subnetwork);
    if (needCreate = connections == null) {
      connections = new Threetuple<>(new HashSet<>(), new TreeSet<>(OpticalFiberOutput.PRIORITY_COMPARATOR), channel);
    }

    if (connection.getTransferDirection() == OpticalFiberConnection.TransferDirection.EXTRACT) {
      Set<OpticalFiberInput> inputConnections = connections.x;
      inputConnections.add((OpticalFiberInput) connection);
    } else {
      Set<OpticalFiberOutput> outputConnections = connections.y;
      outputConnections.add((OpticalFiberOutput) connection);
    }

    if (needCreate) this.connections.put(subnetwork, connections);
  }

  public void addAllConnections(@Nullable Collection<OpticalFiberConnection> connections) {
    if (connections == null) return;
    connections.forEach(this::addConnection);
  }

  /**
   * Removes a connection to the network.
   * @param connection the connection to add.
   * @return a boolean indicating if the connection was in the network.
   */
  public boolean removeConnection(OpticalFiberConnection connection) {
    Tuple<TransferType, String> subnetwork = new Tuple<>(connection.transferType, connection.channelName);
    Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connections = this.connections.get(subnetwork);

    if (connections == null) return false;
    if (connection.getTransferDirection() == OpticalFiberConnection.TransferDirection.EXTRACT) {
      if (!connections.x.remove((OpticalFiberInput) connection)) return false;
    } else {
      if (!connections.y.remove((OpticalFiberOutput) connection)) return false;
    }

    if (connections.z.markUnuse()) {
      this.existingChannels.get(ModTransferTypes.getIndex(connection.transferType)).remove(connections.z);
    }

    return true;
  }

  public boolean removeAllConnections(Collection<OpticalFiberConnection> connections) {
    boolean success = true;
    for (OpticalFiberConnection connection : connections) {
      success &= this.removeConnection(connection);
    }
    return success;
  }

  /**
   * Gets the channels that exist for a specific channel type on the network
   * @param transferType The transfer type
   * @return The list of channels sorted from most recently used to least recently used
   */
  public List<String> getChannels(TransferType transferType) {
    List<Channel> channels = this.existingChannels.get(ModTransferTypes.getIndex(transferType));
    channels.sort(CHANNEL_DISPLAY_PRIORITY_COMPARATOR.reversed());
    List<String> channelNames = new ArrayList<>(channels.size());
    channels.forEach(channel -> channelNames.add(channel.channelName));
    return channelNames;
  }

  /**
   * Carries out a single transfer from each input to every output to which it is connected
   * @param world The world
   */
  public void doTransfers(World world) {
    for (Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> subnetworkConnections : connections.values()) {
      if (subnetworkConnections == null || subnetworkConnections.x == null) continue;
      for (OpticalFiberInput input : subnetworkConnections.x) {
        if (!input.isOffering(world) || subnetworkConnections.y == null)
          continue;
        Iterator<OpticalFiberOutput> iterator = subnetworkConnections.y.descendingIterator();
        while (iterator.hasNext()) {
          // Do a single transfer for each input
          if (input.doTransfer(world, iterator.next())) break;
        }
      }
    }
  }

  /**
   * Imports all connections from a distinct network.
   * @param oldNetwork the distinct network to be cannibalized.
   */
  public void cannibalize(FiberNetwork oldNetwork) {
    for (Tuple<TransferType, String> subnetwork : oldNetwork.connections.keySet()) {
      Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> oldConnections = oldNetwork.connections.get(subnetwork);
      Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connections = this.connections.get(subnetwork);

      if (connections == null) {
        this.connections.put(subnetwork, oldConnections);
        this.existingChannels.get(ModTransferTypes.getIndex(subnetwork.x)).add(oldConnections.z);
      } else {
        connections.x.addAll(oldConnections.x);
        connections.y.addAll(oldConnections.y);
        connections.z.cannibalize(oldConnections.z);
      }
    }
  }

  public NBTTagCompound serializeNBT() {
    NBTTagCompound compound = new NBTTagCompound();
    NBTTagCompound channelsNBT = new NBTTagCompound();
    for (TransferType type : ModTransferTypes.VALUES) {
      List<Channel> channelsForType = this.existingChannels.get(ModTransferTypes.getIndex(type));
      if (channelsForType.size() > 0) {
        NBTTagCompound channelsForTypeNBT = new NBTTagCompound();
        for (Channel channel : channelsForType) {
          NBTTagCompound channelNBT = new NBTTagCompound();
          channelNBT.setInteger("num_uses", channel.numUses);
          channelNBT.setLong("last_used", channel.lastUsed.toEpochMilli());
          channelsForTypeNBT.setTag(channel.channelName, channelNBT);
        }
        channelsNBT.setTag(type.getUnlocalizedName(), channelsForTypeNBT);
      }
    }
    compound.setTag("channels", channelsNBT);

    NBTTagList inputs = new NBTTagList();
    for (Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connectionGroup : this.connections.values()) {
      if (!connectionGroup.x.isEmpty()) {
        for (OpticalFiberInput input : connectionGroup.x) {
          inputs.appendTag(input.serializeNBT());
        }
      }
    }
    compound.setTag("inputs", inputs);

    NBTTagList outputs = new NBTTagList();
    for (Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> connectionGroup : this.connections.values()) {
      if (!connectionGroup.y.isEmpty()) {
        for (OpticalFiberOutput output : connectionGroup.y) {
          outputs.appendTag(output.serializeNBT());
        }
      }
    }
    compound.setTag("outputs", outputs);

    return compound;
  }

  public void deserializeNBT(NBTTagCompound compound) {
    NBTTagCompound channelsNBT = compound.getCompoundTag("channels");
    for (String typeName : channelsNBT.getKeySet()) {
      NBTTagCompound channelsForTypeNBT = channelsNBT.getCompoundTag(typeName);
      for (String channelName : channelsForTypeNBT.getKeySet()) {
        NBTTagCompound channelNBT = channelsForTypeNBT.getCompoundTag(channelName);
        int numUses = channelNBT.getInteger("num_uses");
        long lastUsedMillis = channelNBT.getLong("last_used");
        Channel channel = new Channel(channelName, numUses, Instant.ofEpochMilli(lastUsedMillis));
        this.existingChannels.get(ModTransferTypes.getIndex(ModTransferTypes.fromUnlocalizedName(typeName))).add(channel);
      }
    }

    NBTTagList inputsNBT = compound.getTagList("inputs", Constants.NBT.TAG_COMPOUND);
    inputsNBT.iterator().forEachRemaining(inputNBT -> {
      this.restoreConnection(new OpticalFiberInput((NBTTagCompound) inputNBT));
    });

    NBTTagList outputsNBT = compound.getTagList("outputs", Constants.NBT.TAG_COMPOUND);
    outputsNBT.iterator().forEachRemaining(outputNBT -> {
      this.restoreConnection(new OpticalFiberOutput((NBTTagCompound) outputNBT));
    });
  }

  private static final Comparator<Channel> CHANNEL_DISPLAY_PRIORITY_COMPARATOR = (t0, t1) -> {
    int cmp;
    // Provide consistency with equals
    cmp = t0.lastUsed.compareTo(t1.lastUsed);
    if (cmp != 0) return cmp;
    cmp = Integer.compare(t0.numUses, t1.numUses);
    if (cmp != 0) return cmp;
    return t0.channelName.compareTo(t1.channelName);
  };

  private class Channel {
    final String channelName;
    private Instant lastUsed;
    private int numUses;

    Channel(String channelName) {
      this(channelName, 0, Instant.now());
    }

    Channel(String channelName, int numUses, Instant lastUsed) {
      this.channelName = channelName;
      this.numUses = numUses;
      this.lastUsed = lastUsed;
    }

    void markUse() {
      ++numUses;
      lastUsed = Instant.now();
    }

    boolean markUnuse() {
      return --numUses == 0;
    }

    void cannibalize(Channel channel) {
      assert this.channelName.equals(channel.channelName);
      this.numUses += channel.numUses;
      if (this.lastUsed.compareTo(channel.lastUsed) < 0)
        this.lastUsed = channel.lastUsed;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Channel)) return false;
      return channelName.equals(((Channel)o).channelName);
    }
  }

  private final class Tuple<X, Y> {
    final X x;
    final Y y;

    Tuple(X x, Y y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple<?, ?> tuple = (Tuple<?, ?>) o;
      return Objects.equals(x, tuple.x) &&
              Objects.equals(y, tuple.y);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }

    @Override
    public String toString() {
      return "("+ x + ", " + y + ')';
    }
  }

  private final class Threetuple<X, Y, Z> {
    final X x;
    final Y y;
    final Z z;

    Threetuple(X x, Y y, Z z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Threetuple<?, ?, ?> that = (Threetuple<?, ?, ?>) o;
      return Objects.equals(x, that.x) &&
              Objects.equals(y, that.y) &&
              Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
      return "("+ x + ", " + y + ", " + z + ')';
    }
  }
}
