package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class KickMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public KickMessage(Channel channel, User user)
    {
        this(channel.name, user.getNick());
    }

    public KickMessage(String channel, String user)
    {
        this(channel, user, "");
    }

    public KickMessage(Channel channel, User user, String reason)
    {
        this(channel.name, user.getNick(), reason);
    }

    public KickMessage(String channel, String user, String reason)
    {
        super("KICK " + channel + " " + user +  " :" + reason);
    }
}
