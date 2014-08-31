package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class KickAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public KickAction(Channel channel, User user)
    {
        this(channel.name, user.getNick());
    }

    public KickAction(String channel, String user)
    {
        this(channel, user, "");
    }

    public KickAction(Channel channel, User user, String reason)
    {
        this(channel.name, user.getNick(), reason);
    }

    public KickAction(String channel, String user, String reason)
    {
        super("KICK " + channel + " " + user +  " :" + reason);
    }
}
