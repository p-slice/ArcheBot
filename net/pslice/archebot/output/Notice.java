package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;
import net.pslice.archebot.User;

public final class Notice extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public Notice(Channel channel, String notice, Object... objects)
    {
        this(channel.name, notice, objects);
    }

    public Notice(User user, String notice, Object... objects)
    {
        this(user.getNick(), notice, objects);
    }

    public Notice(String target, String notice, Object... objects)
    {
        super("NOTICE " + target + " :" + String.format(notice, objects));
    }
}