package net.pslice.archebot.events;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.User;

public class PrivateNoticeEvent<B extends ArcheBot> extends ArcheBot.Event<B> {

    /*
     * =======================================
     * Objects and variables:
     * =======================================
     */

    private final User user;
    private final String notice;

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PrivateNoticeEvent(B bot, User user, String notice)
    {
        super(bot);

        this.user = user;
        this.notice = notice;
    }

    /*
     * =======================================
     * Public methods:
     * =======================================
     */

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

    public static interface Listener<B extends ArcheBot> extends ArcheBot.Listener<B>
    {
        public void onPrivateNotice(PrivateNoticeEvent<B> event);
    }
}
