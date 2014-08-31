package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class MessageAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public MessageAction(Channel channel, String message)
    {
        this(channel.name, message);
    }

    public MessageAction(User user, String message)
    {
        this(user.getNick(), message);
    }

    public MessageAction(String target, String message)
    {
        super("PRIVMSG " + target + " :" + message);
    }
}
