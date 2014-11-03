package net.pslice.archebot.output;

import net.pslice.archebot.Channel;
import net.pslice.archebot.IrcAction;

public final class PartMessage extends IrcAction {

    /*
     * =======================================
     * Constructors:
     * =======================================
     */

    public PartMessage(Channel channel)
    {
        this(channel.name);
    }

    public PartMessage(String channel)
    {
        this(channel, "");
    }

    public PartMessage(Channel channel, String reason, Object... objects)
    {
        this(channel.name, reason, objects);
    }

    public PartMessage(String channel, String reason, Object... objects)
    {
        super("PART " + channel + " :" + String.format(reason, objects));
    }
}
