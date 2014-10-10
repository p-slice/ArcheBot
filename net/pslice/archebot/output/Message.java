package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class Message extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Message(Channel channel, String message)
    {
        this(channel.name, message);
    }

    public Message(User user, String message)
    {
        this(user.getNick(), message);
    }

    public Message(String target, String message)
    {
        super("PRIVMSG " + target + " :" + message);
    }
}
