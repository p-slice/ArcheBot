package net.pslice.archebot.output;

import net.pslice.archebot.Channel;

public final class JoinMessage extends Output {

    public JoinMessage(Channel channel) {
        this(channel.name);
    }

    public JoinMessage(Channel channel, String key) {
        this(channel.name, key);
    }

    public JoinMessage(String channel) {
        super("JOIN " + channel);
    }

    public JoinMessage(String channel, String key) {
        super("JOIN " + channel + " " + key);
    }
}
