package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class ActionMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public ActionMessage(Channel channel, String action)
    {
        this(channel.name, action);
    }

    public ActionMessage(User user, String action)
    {
        this(user.getNick(), action);
    }

    public ActionMessage(String target, String action)
    {
        super("PRIVMSG " + target + " :\u0001ACTION " + action + "\u0001");
    }
}
