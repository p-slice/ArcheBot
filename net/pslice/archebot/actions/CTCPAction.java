package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;
import net.pslice.archebot.User;

public final class CTCPAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public CTCPAction(Channel channel, String command)
    {
        this(channel.name, command);
    }

    public CTCPAction(User user, String command)
    {
        this(user.getNick(), command);
    }

    public CTCPAction(String target, String command)
    {
        this(target, command, "");
    }

    public CTCPAction(Channel channel, String command, String message)
    {
        this(channel.name, command, message);
    }

    public CTCPAction(User user, String command, String message)
    {
        this(user.getNick(), command, message);
    }

    public CTCPAction(String target, String command, String message)
    {
        super("PRIVMSG " + target + " :\u0001" + command.toUpperCase() + (message.equals("") ? "" : " " + message) + "\u0001");
    }
}
