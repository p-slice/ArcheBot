package net.pslice.archebot.output;

import net.pslice.archebot.Channel;

public final class TopicMessage extends Output {

    public TopicMessage(Channel channel, String topic) {
        this(channel.getName(), topic);
    }

    public TopicMessage(String channel, String topic) {
        super("TOPIC " + channel + " :" + topic);
    }
}
