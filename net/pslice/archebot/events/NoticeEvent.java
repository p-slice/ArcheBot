package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.Event;
import net.pslice.archebot.User;

public class NoticeEvent<B extends ArcheBot> extends Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final Channel channel;
    private final User user;
    private final String notice;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NoticeEvent(B bot, Channel channel, User user, String notice)
    {
        super(bot);

        this.channel = channel;
        this.user = user;
        this.notice = notice;
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

    public String getNotice()
    {
        return notice;
    }

    /*
     * =======================================
     * Internal classes:
     * =======================================
     */

    public static interface Listener<B extends ArcheBot> extends net.pslice.archebot.Listener<B>
    {
        public void onNotice(NoticeEvent<B> event);
    }
}
