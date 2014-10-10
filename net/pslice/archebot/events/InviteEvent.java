package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Event;
import net.pslice.archebot.User;

public class InviteEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public InviteEvent(B bot, Channel channel, User user)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
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

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onInvite(InviteEvent<B> event);
    }
}
