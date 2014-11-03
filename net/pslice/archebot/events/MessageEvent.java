package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public class MessageEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final String message;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public MessageEvent(B bot, Channel channel, User user, String message)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.message = message;
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

    public String getMessage()
    {
        return message;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onMessage(MessageEvent<B> event);
    }
}
