package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Action extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Action(Channel channel, String action, Object... objects)
    {
        this(channel.name, action, objects);
    }

    public Action(User user, String action, Object... objects)
    {
        this(user.getNick(), action, objects);
    }

    public Action(String target, String action, Object... objects)
    {
        super("PRIVMSG " + target + " :\u0001ACTION " + String.format(action, objects) + "\u0001");
    }
}
