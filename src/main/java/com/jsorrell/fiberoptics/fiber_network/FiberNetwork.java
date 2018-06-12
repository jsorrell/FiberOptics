package com.jsorrell.fiberoptics.fiber_network;

import com.jsorrell.fiberoptics.FiberOptics;
import com.jsorrell.fiberoptics.fiber_network.connection.OpticalFiberConnection;
import com.jsorrell.fiberoptics.fiber_network.type.TransferType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FiberNetwork implements INBTSerializable<NBTTagCompound> {
  // TODO these need to be parallelized
  private Map<String, Channel> existingChannels = new TreeMap<>();
  private Map<Subnetwork, Tuple<TreeSet<OpticalFiberConnection>, Channel>> connections = new HashMap<>();

  public FiberNetwork() {
    // Initialize lists in existingChannels
  }

  /**
   * Adds a connection to the network.
   * @param connection the connection to add.
   */
  public void addConnection(OpticalFiberConnection connection) {
    // Update connections
    Subnetwork subnetwork = new Subnetwork(connection);
    Tuple<TreeSet<OpticalFiberConnection>, Channel> connections = this.connections.get(subnetwork);

    if (connections == null) {
      Channel channel = this.existingChannels.getOrDefault(connection.channelName, new Channel(connection.channelName));
      connections = new Tuple<>(new TreeSet<>(Comparator.naturalOrder()), channel);
      // Add channel to existingChannels
      this.existingChannels.put(connection.channelName, channel);
      this.connections.put(subnetwork, connections);
    }

    connections.getFirst().add(connection);
    connections.getSecond().markUse();
  }

  /**
   * Same as {@link FiberNetwork#addConnection(OpticalFiberConnection)} but assumes the channel has already been added to {@link FiberNetwork#existingChannels}.
   * Doesn't modify {@link FiberNetwork#existingChannels} or any {@link Channel}.
   * @param connection the connection to add.
   */
  private void restoreConnection(OpticalFiberConnection connection) {
    Channel channel = this.existingChannels.get(connection.channelName);
    if (channel == null) throw new AssertionError("restoreConnection used incorrectly. Cannot get " + connection.channelName);

    // Update connections
    Subnetwork subnetwork = new Subnetwork(connection);
    Tuple<TreeSet<OpticalFiberConnection>, Channel> connections = this.connections.getOrDefault(subnetwork, new Tuple<>(new TreeSet<>(), channel));
    this.connections.putIfAbsent(subnetwork, connections);
    connections.getFirst().add(connection);
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
    Subnetwork subnetwork = new Subnetwork(connection);
    Tuple<TreeSet<OpticalFiberConnection>, Channel> connections = this.connections.get(subnetwork);

    if (connections == null) return false;
    if (!connections.getFirst().remove(connection)) return false;

    if (connections.getSecond().markUnuse()) {
      this.existingChannels.remove(connection.channelName);
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
   * Gets the channels that exist on the network
   * @return The list of channels sorted from most recently used to least recently used
   */
  public List<String> getChannels() {
    return this.existingChannels.values().stream().sorted(Channel.MOST_RECENTLY_USED_FIRST).map(c -> c.channelName).collect(Collectors.toList());
  }

  /**
   * Carries out a single transfer from each input to every output to which it is connected
   * @param world The world
   */
//  public void doTransfers(World world) {
//    for (Threetuple<Set<OpticalFiberInput>, TreeSet<OpticalFiberOutput>, Channel> subnetworkConnections : connections.values()) {
//      if (subnetworkConnections == null || subnetworkConnections.x == null) continue;
//      for (OpticalFiberInput input : subnetworkConnections.x) {
//        if (!input.isOffering(world) || subnetworkConnections.y == null)
//          continue;
//        Iterator<OpticalFiberOutput> iterator = subnetworkConnections.y.descendingIterator();
//        while (iterator.hasNext()) {
//          // Do a single transfer for each input
//          if (input.doTransfer(world, iterator.next())) break;
//        }
//      }
//    }
//  }

  /**
   * Imports all connections from a distinct network.
   * @param oldNetwork the distinct network to be cannibalized.
   */
  public void cannibalize(FiberNetwork oldNetwork) {
    Map<Subnetwork, Tuple<TreeSet<OpticalFiberConnection>, Channel>> sourceConnections;
    Map<Subnetwork, Tuple<TreeSet<OpticalFiberConnection>, Channel>> destinationConnections;
    Map<String, Channel> destinationChannels;

    if (oldNetwork.connections.size() < this.connections.size()) {
      sourceConnections = oldNetwork.connections;
      destinationConnections = this.connections;
      destinationChannels = this.existingChannels;
    } else {
      sourceConnections = this.connections;
      destinationConnections = oldNetwork.connections;
      destinationChannels = oldNetwork.existingChannels;
    }

    for (Map.Entry<Subnetwork, Tuple<TreeSet<OpticalFiberConnection>, Channel>> pair : sourceConnections.entrySet()) {
      Subnetwork subnetwork = pair.getKey();
      Tuple<TreeSet<OpticalFiberConnection>, Channel> connections1 = pair.getValue();
      Tuple<TreeSet<OpticalFiberConnection>, Channel> connections2 = destinationConnections.get(subnetwork);
      Channel channel1 = connections1.getSecond();
      Channel channel2 = destinationChannels.get(channel1.channelName);
      Channel destinationChannel = channel2 == null ? channel1 : Channel.merge(channel1, channel2);


      if (connections2 == null) {
        destinationConnections.put(subnetwork, new Tuple<>(connections1.getFirst(), destinationChannel));
      } else {
        TreeSet<OpticalFiberConnection> smallerMap;
        TreeSet<OpticalFiberConnection> largerMap;
        if (connections1.getFirst().size() < connections2.getFirst().size()) {
          smallerMap = connections1.getFirst();
          largerMap = connections2.getFirst();
        } else {
          smallerMap = connections2.getFirst();
          largerMap = connections1.getFirst();
        }

        largerMap.addAll(smallerMap);
        destinationConnections.put(subnetwork, new Tuple<>(largerMap, destinationChannel));
      }
    }

    this.existingChannels = destinationChannels;
    this.connections = destinationConnections;
  }

  public NBTTagCompound serializeNBT() {
    NBTTagCompound compound = new NBTTagCompound();
    /* Channels */
    NBTTagList channelsNBT = new NBTTagList();
    compound.setTag("Channels", channelsNBT);

    for (Channel channel : this.existingChannels.values()) {
      NBTTagCompound channelNBT = new NBTTagCompound();
      channelsNBT.appendTag(channelNBT);
      channelNBT.setString("Name", channel.channelName);
      channelNBT.setInteger("NumUses", channel.numUses);
      channelNBT.setLong("LastUsed", channel.lastUsed.toEpochMilli());
    }

    /* Connections */
    NBTTagList connections = new NBTTagList();
    compound.setTag("Connections", connections);

    for (Tuple<TreeSet<OpticalFiberConnection>, Channel> connectionGroup : this.connections.values()) {
      for (OpticalFiberConnection connection : connectionGroup.getFirst()) {
        connections.appendTag(OpticalFiberConnection.serializeNBT(connection));
      }
    }

    return compound;
  }

  public void deserializeNBT(NBTTagCompound compound) {
    /* Channels */
    NBTTagList channelsNBT = compound.getTagList("Channels", Constants.NBT.TAG_COMPOUND);

    for (NBTBase channelNBTBase : channelsNBT) {
      NBTTagCompound channelNBT = (NBTTagCompound) channelNBTBase;
      String channelName = channelNBT.getString("Name");
      int numUses = channelNBT.getInteger("NumUses");
      Instant lastUsed = Instant.ofEpochMilli(channelNBT.getLong("LastUsed"));
      Channel channel = new Channel(channelName, numUses, lastUsed);
      this.existingChannels.put(channelName, channel);
    }

    /* Connections */
    NBTTagList connectionsNBT = compound.getTagList("Connections", Constants.NBT.TAG_COMPOUND);
    for (NBTBase connectionNBT : connectionsNBT) {
      OpticalFiberConnection connection;
      try {
        connection = OpticalFiberConnection.fromNBT((NBTTagCompound) connectionNBT);
      } catch (OpticalFiberConnection.InvalidTypeKeyException | OpticalFiberConnection.InvalidConnectionKeyException e) {
        /* Mark one less usage for the channel */
        String channelName = ((NBTTagCompound) connectionNBT).getString("ChannelName");
        Channel channel = this.existingChannels.get(channelName);
        if (channel != null && channel.markUnuse()) this.existingChannels.remove(channelName);
        // Don't restore these connections. Just remove them from the world by not restoring.
        FiberOptics.LOGGER.log(Level.WARNING, e.toString());
        return;
      }
      this.restoreConnection(connection);
    }
  }


  private static class Channel {
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

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Channel)) return false;
      return channelName.equals(((Channel)o).channelName);
    }

    public static Channel merge(Channel channel1, Channel channel2) {
      assert channel1.channelName.equals(channel2.channelName);
      channel1.numUses += channel2.numUses;
      if (channel1.lastUsed.compareTo(channel2.lastUsed) < 0)
        channel1.lastUsed = channel2.lastUsed;
      return channel1;
    }

    private static final Comparator<Channel> MOST_RECENTLY_USED_FIRST = (t0, t1) -> {
      int cmp;
      // Provide consistency with equals
      cmp = t1.lastUsed.compareTo(t0.lastUsed);
      if (cmp != 0) return cmp;
      cmp = Integer.compare(t1.numUses, t0.numUses);
      if (cmp != 0) return cmp;
      return t0.channelName.compareTo(t1.channelName);
    };
  }

  private static class Subnetwork {
    private final TransferType type;
    private final String channel;

    private Subnetwork(TransferType type, String channel) {
      this.type = type;
      this.channel = channel;
    }

    private Subnetwork(OpticalFiberConnection connection) {
      this(connection.getTransferType(), connection.channelName);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Subnetwork)) return false;
      Subnetwork that = (Subnetwork) o;
      return Objects.equals(type, that.type) &&
              Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, channel);
    }

    @Override
    public String toString() {
      return "(" + type.toString() + " on channel \"" + channel + "\")";
    }
  }
}
