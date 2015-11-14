package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class ChannelMap {

    private final TreeMap<String, Channel> channels = new TreeMap<>();

    ChannelMap() {}

    public Channel getChannel(String name) {
        return isChannel(name) ? channels.get(name.toLowerCase()) : new Channel(name);
    }

    public TreeSet<String> getChannelNames() {
        return new TreeSet<>(channels.keySet());
    }

    public TreeSet<Channel> getChannels() {
        return new TreeSet<>(channels.values());
    }

    public boolean isChannel(String name) {
        return channels.containsKey(name.toLowerCase());
    }

    void addChannel(Channel channel) {
        channels.put(channel.name.toLowerCase(), channel);
    }

    void removeChannel(String name) {
        channels.remove(name.toLowerCase());
    }
}
