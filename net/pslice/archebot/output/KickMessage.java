package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class KickMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public KickMessage(Channel channel, User user)
    {
        this(channel.name, user.getNick());
    }

    public KickMessage(String channel, String user)
    {
        this(channel, user, "");
    }

    public KickMessage(Channel channel, User user, String reason, Object... objects)
    {
        this(channel.name, user.getNick(), reason, objects);
    }

    public KickMessage(String channel, String user, String reason, Object... objects)
    {
        super("KICK " + channel + " " + user +  " :" + String.format(reason, objects));
    }
}
