package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class TopicAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public TopicAction(Channel channel, String topic)
    {
        this(channel.name, topic);
    }

    public TopicAction(String channel, String topic)
    {
        super("TOPIC " + channel + " :" + topic);
    }
}
