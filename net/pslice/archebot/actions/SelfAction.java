package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class SelfAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public SelfAction(Channel channel, String action)
    {
        this(channel.name, action);
    }

    public SelfAction(User user, String action)
    {
        this(user.getNick(), action);
    }

    public SelfAction(String target, String action)
    {
        super("PRIVMSG " + target + " :\u0001ACTION " + action + "\u0001");
    }
}
