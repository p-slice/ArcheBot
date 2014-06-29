package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class TopicAction extends IrcAction {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private static final TopicAction instance = new TopicAction();

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    private TopicAction() {}

    /*
     * =======================================
     * Static methods:
     * =======================================
     */

    public static TopicAction build(Channel channel, String topic)
    {
        return build(channel.name, topic);
    }

    public static TopicAction build(String channel, String topic)
    {
        instance.setText("TOPIC " + channel + " :" + topic);
        return instance;
    }
}
