package net.pslice.archebot.output;

import net.pslice.archebot.Channel;

public final class TopicMessage extends Output {

    public TopicMessage(Channel channel, String topic, Object... objects) {
        this(channel.getName(), topic, objects);
    }

    public TopicMessage(String channel, String topic, Object... objects) {
        super(String.format("TOPIC %s :%s", channel, objects.length > 0 ? String.format(topic, objects) : topic));
    }
}
