package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Event;
import net.pslice.archebot.User;

public class KickEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User kicker,
                       receiver;
    private final String message;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public KickEvent(B bot, Channel channel, User kicker, User receiver, String message)
    {
        super(bot);

        this.channel = channel;
        this.kicker = kicker;
        this.receiver = receiver;
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

    public User getKicker()
    {
        return kicker;
    }

    public User getReceiver()
    {
        return receiver;
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

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onKick(KickEvent<B> event);
    }
}
