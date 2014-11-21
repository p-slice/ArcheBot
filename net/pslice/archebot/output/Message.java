package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Message extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Message(Channel channel, String message, Object... objects)
    {
        this(channel.name, message, objects);
    }

    public Message(User user, String message, Object... objects)
    {
        this(user.getNick(), message, objects);
    }

    public Message(String target, String message, Object... objects)
    {
        super("PRIVMSG " + target + " :" + String.format(message, objects));
    }
}
