package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class InviteMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public InviteMessage(Channel channel, User user)
    {
        this(channel.name, user.getNick());
    }

    public InviteMessage(String channel, String user)
    {
        super("INVITE " + user + " " + channel);
    }
}
