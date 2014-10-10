package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class NoticeMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public NoticeMessage(Channel channel, String notice)
    {
        this(channel.name, notice);
    }

    public NoticeMessage(User user, String notice)
    {
        this(user.getNick(), notice);
    }

    public NoticeMessage(String target, String notice)
    {
        super("NOTICE " + target + " :" + notice);
    }
}
