package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class TopicMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public TopicMessage(Channel channel, String topic)
    {
        this(channel.name, topic);
    }

    public TopicMessage(String channel, String topic)
    {
        super("TOPIC " + channel + " :" + topic);
    }
}
