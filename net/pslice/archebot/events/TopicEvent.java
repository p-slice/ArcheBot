package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class TopicEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final String topic;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public TopicEvent(B bot, Channel channel, User user, String topic)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.topic = topic;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

    public Channel getChannel()
    {
        return channel;
    }

    public User getUser()
    {
        return user;
    }

    public String getTopic()
    {
        return topic;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onTopicChange(TopicEvent<B> event);
    }
}
