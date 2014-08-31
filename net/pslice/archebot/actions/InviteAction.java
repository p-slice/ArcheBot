package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class InviteAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public InviteAction(Channel channel, User user)
    {
        this(channel.name, user.getNick());
    }

    public InviteAction(String channel, String user)
    {
        super("INVITE " + user + " " + channel);
    }
}
