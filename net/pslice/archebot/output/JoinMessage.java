package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class JoinMessage extends IrcAction {

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
