package net.pslice.archebot.actions;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class PartAction extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PartAction(Channel channel)
    {
        this(channel.name);
    }

    public PartAction(String channel)
    {
        this(channel, "");
    }

    public PartAction(Channel channel, String reason)
    {
        this(channel.name, reason);
    }

    public PartAction(String channel, String reason)
    {
        super("PART " + channel + " :" + reason);
    }
}
