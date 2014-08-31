package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class NoticeAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NoticeAction(Channel channel, String notice)
    {
        this(channel.name, notice);
    }

    public NoticeAction(User user, String notice)
    {
        this(user.getNick(), notice);
    }

    public NoticeAction(String target, String notice)
    {
        super("NOTICE " + target + " :" + notice);
    }
}
