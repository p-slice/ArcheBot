package net.pslice.archebot.output;

import net.pslice.archebot.ArcheBot;
import net.pslice.archebot.Channel;

public final class JoinMessage extends ArcheBot.Output {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public JoinMessage(Channel channel)
    {
        this(channel.name);
    }

    public JoinMessage(String channel)
    {
        super("JOIN " + channel);
    }
}
