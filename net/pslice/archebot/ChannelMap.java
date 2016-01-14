package net.pslice.archebot;

import java.util.TreeMap;
import java.util.TreeSet;

public class ChannelMap {

    private final TreeMap<String, Channel> channels = new TreeMap<>();
    boolean current = true;

    ChannelMap() {}

    public Channel getChannel(String name) {
        return getChannel(name, true);
    }

    public Channel getChannel(String name, boolean createNew) {
        if (isChannel(name))
            return channels.get(name.toLowerCase());
        else if (createNew)
            return new Channel(name);
        else
            throw new RuntimeException("[ChannelMap:getChannel] Attempted to get unknown channel: " + name);
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

    public boolean isChannel(Channel channel) {
        return channels.containsValue(channel);
    }

    public boolean isCurrent() {
        return current;
    }

    public int size() {
        return channels.size();
    }

    void addChannel(Channel channel) {
        channels.put(channel.name.toLowerCase(), channel);
    }

    void removeChannel(String name) {
        channels.remove(name.toLowerCase());
    }
}
