package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class JoinAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public JoinAction(Channel channel)
    {
        this(channel.name);
    }

    public JoinAction(String channel)
    {
        super("JOIN " + channel);
    }
}
