package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class CTCPMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public CTCPMessage(Channel channel, String command)
    {
        this(channel.name, command);
    }

    public CTCPMessage(User user, String command)
    {
        this(user.getNick(), command);
    }

    public CTCPMessage(String target, String command)
    {
        this(target, command, "");
    }

    public CTCPMessage(Channel channel, String command, String message)
    {
        this(channel.name, command, message);
    }

    public CTCPMessage(User user, String command, String message)
    {
        this(user.getNick(), command, message);
    }

    public CTCPMessage(String target, String command, String message)
    {
        super("PRIVMSG " + target + " :\u0001" + command.toUpperCase() + (message.equals("") ? "" : " " + message) + "\u0001");
    }
}
